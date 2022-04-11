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

import com.baremaps.pipeline.config.Config;
import com.baremaps.pipeline.config.Database;
import com.baremaps.pipeline.config.Source;
import com.baremaps.storage.geopackage.GeoPackageStore;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import mil.nga.geopackage.GeoPackageManager;
import org.apache.sis.storage.DataStores;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.Resource;
import org.apache.sis.storage.WritableFeatureSet;
import org.geotoolkit.data.shapefile.ShapefileFeatureStore;
import org.geotoolkit.db.postgres.PostgresStore;
import org.opengis.feature.Feature;

public class Pipeline {

  private Context context;

  private Config config;

  public Pipeline(Context context, Config config) {
    this.context = context;
    this.config = config;
  }

  public void execute() {
    CompletableFuture[] futures = config.getSources().stream()
        .map(this::handle)
        .toArray(CompletableFuture[]::new);
    CompletableFuture.allOf(futures).join();
  }

  public CompletableFuture<Void> handle(Source source) {
    Path downloadFile = downloadFile(source);
    CompletableFuture<Void> steps = CompletableFuture.completedFuture(null);

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
      case "pbf":
        steps = steps.thenRunAsync(() -> importPbf(source));
        break;
      case "shp":
        steps = steps.thenRunAsync(() -> importShp(source));
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
    return context.directory().resolve(source.getId());
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
    try (InputStream inputStream =
        context.blobStore().get(URI.create(source.getUrl())).getInputStream()) {
      Path downloadFile = downloadFile(source);
      Files.createDirectories(downloadFile.getParent());
      Files.copy(inputStream, downloadFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void unzip(Source source) {
    Path sourceDirectory = directory(source);
    Path downloadFile = downloadFile(source);
    try (ZipInputStream zis =
        new ZipInputStream(new BufferedInputStream(Files.newInputStream(downloadFile)))) {
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        Path file = Files.createDirectories(sourceDirectory.resolve(ze.getName()));
        Files.copy(zis, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void importPbf(Source source) {
    Path file = file(source);
  }

  private void importShp(Source source) {
    try {
      Path file = file(source);
      ShapefileFeatureStore aggregate = new ShapefileFeatureStore(file.toUri());
      for (Resource resource : aggregate.components()) {
        if (resource instanceof FeatureSet) {
          var sourceFeatureSet = (FeatureSet) resource;
          importFeatureSet(sourceFeatureSet);
        }
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void importGeoJson(Source source) {
    try {
      Path file = file(source);
      FeatureSet featureSet = (FeatureSet) DataStores.open(file.toUri().toURL());
      importFeatureSet(featureSet);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void importGeoPackage(Source source) {
    try {
      Path file = file(source);
      GeoPackageStore store = new GeoPackageStore(GeoPackageManager.open(file.toFile()));
      for (Resource resource : store.components()) {
        if (resource instanceof FeatureSet) {
          var sourceFeatureSet = (FeatureSet) source;
          importFeatureSet(sourceFeatureSet);
        }
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  private void importFeatureSet(FeatureSet featureSet) {
    Database database = config.getDatabase();
    try (PostgresStore store =
        new PostgresStore(
            database.getHost(),
            database.getPort(),
            database.getName(),
            database.getSchema(),
            database.getUsername(),
            database.getPassword())) {
      var type = featureSet.getType();
      store.createFeatureType(type);
      var target = store.findResource(type.getName().toString());
      if (target instanceof WritableFeatureSet) {
        var targetFeatureSet = (WritableFeatureSet) target;
        try (Stream<Feature> featureStream = featureSet.features(false)) {
          targetFeatureSet.add(featureStream.iterator());
        }
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }
}
