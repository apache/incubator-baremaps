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
import com.baremaps.blob.BlobStoreRouter;
import com.baremaps.blob.HttpBlobStore;
import com.baremaps.pipeline.database.PostgresBaseTest;
import com.baremaps.pipeline.postgres.PostgresUtils;
import com.baremaps.pipeline.steps.FetchUri;
import com.baremaps.pipeline.steps.ImportGeoJson;
import com.baremaps.pipeline.steps.ImportGeoPackage;
import com.baremaps.pipeline.steps.ImportOsmPbf;
import com.baremaps.pipeline.steps.ImportShapefile;
import com.baremaps.pipeline.steps.UnzipFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.db.postgres.PostgresStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

class PipelineTest extends PostgresBaseTest {

  PostgreSQLContainer container;

  @BeforeEach
  public void before() {
    var postgis = DockerImageName.parse("postgis/postgis:13-3.1").asCompatibleSubstituteFor("postgres");
    container = new PostgreSQLContainer(postgis);
    container.start();
  }

  @AfterEach
  public void after() {
    container.stop();
  }

  @Test
  @Disabled
  void execute() throws IOException, DataStoreException {
    var srid = 3857;
    var directory = Files.createTempDirectory(Paths.get("."), "pipeline_");
    var blobStore = new BlobStoreRouter()
        .addScheme("http", new HttpBlobStore())
        .addScheme("https", new HttpBlobStore());

    var databaseHost = container.getHost();
    var databasePort = container.getMappedPort(5432);
    var databaseName = container.getDatabaseName();
    var databaseUsername = container.getUsername();
    var databasePassword = container.getPassword();

    var dataSource = PostgresUtils.dataSource(
        databaseHost,
        databasePort,
        databaseName,
        databaseUsername,
        databasePassword);
    var postgresStore = new PostgresStore(
        databaseHost,
        databasePort,
        databaseName,
        "public",
        databaseUsername,
        databasePassword);

    Context context = new Context() {
      @Override
      public DataSource dataSource() {
        return dataSource;
      }

      @Override
      public BlobStore blobStore() {
        return blobStore;
      }

      @Override
      public PostgresStore postgresStore() {
        return postgresStore;
      }

      @Override
      public Path directory() {
        return directory;
      }

      @Override
      public int srid() {
        return srid;
      }
    };

    Pipeline.builder()
        .setContext(context)
        .addStep(new FetchUri(
            "fetch-geopackage",
            List.of(),
            "https://tiles.baremaps.com/samples/import_db.gpkg",
            "downloads/import_db.gpkg"))
        .addStep(new ImportGeoPackage(
            "import-geopackage",
            List.of("fetch-geopackage"),
            "downloads/import_db.gpkg"))
        .addStep(new FetchUri(
            "fetch-geojson",
            List.of(),
            "https://tiles.baremaps.com/samples/ne_50m_rivers_lake_centerlines_scale_rank.geojson",
            "downloads/ne_50m_rivers_lake_centerlines_scale_rank.geojson"))
        .addStep(new ImportGeoJson(
            "import-geojson",
            List.of("fetch-geojson"),
            "downloads/ne_50m_rivers_lake_centerlines_scale_rank.geojson"))
        .addStep(new FetchUri(
            "fetch-shapefile",
            List.of(),
            "https://tiles.baremaps.com/samples/monaco-shapefile.zip",
            "downloads/monaco-shapefile.zip"))
        .addStep(new UnzipFile(
            "unzip-shapefile",
            List.of("fetch-shapefile"),
            "downloads/monaco-shapefile.zip",
            "archives/monaco"))
        .addStep(new ImportShapefile(
            "import-shapefile",
            List.of("fetch-geojson"),
            "archives/monaco/gis_osm_buildings_a_free_1.shp"))
        .addStep(new FetchUri(
            "fetch-osmpbf",
            List.of(),
            "https://tiles.baremaps.com/samples/liechtenstein.osm.pbf",
            "downloads/liechtenstein.osm.pbf"))
        .addStep(new ImportOsmPbf(
            "import-osmpbf",
            List.of("fetch-osmpbf"),
            "downloads/liechtenstein.osm.pbf"))
        .build().execute().join();

    Files.walk(directory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }
}
