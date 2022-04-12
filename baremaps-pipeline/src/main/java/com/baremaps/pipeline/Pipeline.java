/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.pipeline;

import com.baremaps.blob.BlobStore;
import com.baremaps.collection.AlignedDataList;
import com.baremaps.collection.LongDataMap;
import com.baremaps.collection.LongDataSortedMap;
import com.baremaps.collection.LongSizedDataDenseMap;
import com.baremaps.collection.memory.OnDiskDirectoryMemory;
import com.baremaps.collection.type.LonLatDataType;
import com.baremaps.collection.type.LongDataType;
import com.baremaps.collection.type.LongListDataType;
import com.baremaps.collection.type.PairDataType;
import com.baremaps.collection.utils.FileUtils;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.pipeline.config.Config;
import com.baremaps.pipeline.config.Source;
import com.baremaps.pipeline.database.ImportService;
import com.baremaps.pipeline.database.repository.HeaderRepository;
import com.baremaps.pipeline.database.repository.PostgresHeaderRepository;
import com.baremaps.pipeline.database.repository.PostgresNodeRepository;
import com.baremaps.pipeline.database.repository.PostgresRelationRepository;
import com.baremaps.pipeline.database.repository.PostgresWayRepository;
import com.baremaps.pipeline.database.repository.Repository;
import com.baremaps.pipeline.postgres.PostgresUtils;
import com.baremaps.storage.geopackage.GeoPackageStore;
import java.io.BufferedInputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.sql.DataSource;
import mil.nga.geopackage.GeoPackageManager;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.DataStores;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.Resource;
import org.apache.sis.storage.WritableFeatureSet;
import org.geotoolkit.data.shapefile.ShapefileFeatureStore;
import org.geotoolkit.db.postgres.PostgresStore;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Feature;

public class Pipeline implements AutoCloseable {

  private BlobStore blobStore;

  private Path directory;

  private Config config;

  private PostgresStore dataStore;

  public Pipeline(BlobStore blobStore, Path directory, Config config) {
    this.blobStore = blobStore;
    this.directory = directory;
    this.config = config;
    this.dataStore = initDataStore(config);
  }

  private PostgresStore initDataStore(Config config) {
    try {
      return new PostgresStore(
          config.getDatabase().getHost(),
          config.getDatabase().getPort(),
          config.getDatabase().getName(),
          config.getDatabase().getSchema(),
          config.getDatabase().getUsername(),
          config.getDatabase().getPassword());
    } catch (DataStoreException e) {
      throw new PipelineException(e);
    }
  }

  private DataSource initDataSource() {
    DataSource datasource = PostgresUtils.datasource(
        config.getDatabase().getHost(),
        config.getDatabase().getPort(),
        config.getDatabase().getName(),
        config.getDatabase().getUsername(),
        config.getDatabase().getPassword());
    return datasource;
  }

  public void execute() {
    var futures = config.getSources().stream()
        .map(this::handle)
        .toArray(CompletableFuture[]::new);
    CompletableFuture.allOf(futures).join();
  }

  public CompletableFuture<Void> handle(Source source) {
    CompletableFuture<Void> steps = CompletableFuture.completedFuture(null);
    var downloadFile = downloadFile(source);

    // download file
    if (!Files.exists(downloadFile)) {
      steps = steps.thenRunAsync(() -> download(source));
    }

    // expand file
    if ("zip".equals(source.getArchive())) {
      steps = steps.thenRunAsync(() -> unzip(source));
    }

    // import file
    switch (source.getFormat()) {
      case "osmpbf":
        steps = steps.thenRunAsync(() -> importOsmPbf(source));
        break;
      case "shapefile":
        steps = steps.thenRunAsync(() -> importShapefile(source));
        break;
      case "geojson":
        steps = steps.thenRunAsync(() -> importGeoJson(source));
        break;
      case "geopackage":
        steps = steps.thenRunAsync(() -> importGeoPackage(source));
        break;
    }

    return steps;
  }

  private Path directory(Source source) {
    return directory.resolve(source.getId());
  }

  private Path archiveFile(Source source) {
    return directory(source).resolve(source.getFile());
  }

  private Path downloadFile(Source source) {
    var uri = URI.create(source.getUrl()).getPath();
    var name = Paths.get(uri).getFileName();
    return directory(source).resolve(name);
  }

  private Path file(Source source) {
    if (source.getFile() == null) {
      return downloadFile(source);
    } else {
      return archiveFile(source);
    }
  }

  private void download(Source source) {
    try (var inputStream = blobStore.get(URI.create(source.getUrl())).getInputStream()) {
      var downloadFile = downloadFile(source);
      Files.createDirectories(downloadFile.getParent());
      Files.copy(inputStream, downloadFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void unzip(Source source) {
    var sourceDirectory = directory(source);
    var downloadFile = downloadFile(source);
    try (var zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(downloadFile)))) {
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        var file = Files.createDirectories(sourceDirectory.resolve(ze.getName()));
        Files.copy(zis, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void importOsmPbf(Source source) {
    try {
      Path file = file(source);

      DataSource datasource = initDataSource();
      HeaderRepository headerRepository = new PostgresHeaderRepository(datasource);
      Repository<Long, Node> nodeRepository = new PostgresNodeRepository(datasource);
      Repository<Long, Way> wayRepository = new PostgresWayRepository(datasource);
      Repository<Long, Relation> relationRepository = new PostgresRelationRepository(datasource);

      headerRepository.drop();
      nodeRepository.drop();
      wayRepository.drop();
      relationRepository.drop();

      headerRepository.create();
      nodeRepository.create();
      wayRepository.create();
      relationRepository.create();

      Path directory = Files.createTempDirectory(Paths.get("."), "baremaps_");
      Path nodes = Files.createDirectories(directory.resolve("nodes"));
      Path referencesKeys = Files.createDirectories(directory.resolve("references_keys"));
      Path referencesValues = Files.createDirectories(directory.resolve("references_values"));

      LongDataMap<Coordinate> coordinates =
          new LongSizedDataDenseMap<>(
              new LonLatDataType(),
              new OnDiskDirectoryMemory(nodes));
      LongDataMap<List<Long>> references =
          new LongDataSortedMap<>(
              new AlignedDataList<>(
                  new PairDataType<>(
                      new LongDataType(),
                      new LongDataType()),
                  new OnDiskDirectoryMemory(referencesKeys)),
              new com.baremaps.collection.DataStore<>(new LongListDataType(),
                  new OnDiskDirectoryMemory(referencesValues)));

      new ImportService(
          file.toUri(),
          blobStore,
          coordinates,
          references,
          headerRepository,
          nodeRepository,
          wayRepository,
          relationRepository,
          config.getDatabase().getSrid())
          .call();

      FileUtils.deleteRecursively(directory);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }


  private void importShapefile(Source source) {
    try {
      var file = file(source);
      var aggregate = new ShapefileFeatureStore(file.toUri());
      for (var resource : aggregate.components()) {
        if (resource instanceof FeatureSet featureSet) {
          importFeatureSet(featureSet);
        }
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void importGeoJson(Source source) {
    try {
      var file = file(source);
      var featureSet = (FeatureSet) DataStores.open(file.toUri().toURL());
      importFeatureSet(featureSet);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void importGeoPackage(Source source) {
    try {
      var file = file(source);
      var store = new GeoPackageStore(GeoPackageManager.open(file.toFile()));
      for (Resource resource : store.components()) {
        if (resource instanceof FeatureSet featureSet) {
          importFeatureSet(featureSet);
        }
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void importFeatureSet(FeatureSet featureSet) {
    try {
      var type = featureSet.getType();
      dataStore.deleteFeatureType(type.getName().toString());
      dataStore.createFeatureType(type);
      var target = dataStore.findResource(type.getName().toString());
      if (target instanceof WritableFeatureSet writableFeatureSet) {
        try (var featureStream = featureSet.features(false)) {
          writableFeatureSet.add(featureStream.map(this::reproject).iterator());
        }
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private Feature reproject(Feature feature) {
    for (var property : feature.getType().getProperties(false)) {
      String name = property.getName().toString();
      if (feature.getPropertyValue(name) instanceof Geometry geometry) {
        Geometry value = new ProjectionTransformer(geometry.getSRID(), config.getDatabase().getSrid()).transform(geometry);
        feature.setPropertyValue(name, value);
      }
    }
    return feature;
  }

  @Override
  public void close() throws Exception {
    dataStore.close();
  }

}
