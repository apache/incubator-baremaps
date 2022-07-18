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

package com.baremaps.workflow;

import com.baremaps.workflow.model.Database;
import com.baremaps.workflow.tasks.DownloadUrl;
import com.baremaps.workflow.tasks.ImportGeoPackage;
import com.baremaps.workflow.tasks.ImportOsmPbf;
import com.baremaps.workflow.tasks.ImportShapefile;
import com.baremaps.workflow.tasks.UnzipFile;
import java.io.IOException;
import java.util.List;
import org.apache.sis.storage.DataStoreException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

class WorkflowTest extends PostgresBaseTest {

  PostgreSQLContainer container;

  @BeforeEach
  public void before() {
    var postgis =
        DockerImageName.parse("postgis/postgis:13-3.1").asCompatibleSubstituteFor("postgres");
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
    var database =
        new Database(
            container.getHost(),
            container.getMappedPort(5432),
            container.getDatabaseName(),
            "public",
            container.getUsername(),
            container.getPassword());

    WorkflowExecutor.builder()
        .addStep(
            new DownloadUrl(
                "fetch-geopackage",
                List.of(),
                "https://tiles.baremaps.com/samples/import_db.gpkg",
                "downloads/import_db.gpkg"))
        .addStep(
            new ImportGeoPackage(
                "import-geopackage",
                List.of("fetch-geopackage"),
                "downloads/import_db.gpkg",
                database,
                4326,
                3857))
        .addStep(
            new DownloadUrl(
                "fetch-osmpbf",
                List.of(),
                "https://tiles.baremaps.com/samples/liechtenstein.osm.pbf",
                "downloads/liechtenstein.osm.pbf"))
        .addStep(
            new ImportOsmPbf(
                "import-osmpbf",
                List.of("fetch-osmpbf"),
                "downloads/liechtenstein.osm.pbf",
                database,
                4326,
                3857))
        .addStep(
            new DownloadUrl(
                "fetch-shapefile",
                List.of(),
                "https://osmdata.openstreetmap.de/download/simplified-water-polygons-split-3857.zip",
                "downloads/simplified-water-polygons-split-3857.zip"))
        .addStep(
            new UnzipFile(
                "unzip-shapefile",
                List.of("fetch-shapefile"),
                "downloads/simplified-water-polygons-split-3857.zip",
                "archives"))
        .addStep(
            new DownloadUrl(
                "fetch-projection",
                List.of("unzip-shapefile"),
                "https://spatialreference.org/ref/sr-org/epsg3857/prj/",
                "archives/simplified-water-polygons-split-3857/simplified_water_polygons.prj"))
        .addStep(
            new ImportShapefile(
                "import-shapefile",
                List.of("fetch-projection"),
                "archives/simplified-water-polygons-split-3857/simplified_water_polygons.shp",
                database,
                3857,
                3857))
        .build()
        .execute()
        .join();

  }
}
