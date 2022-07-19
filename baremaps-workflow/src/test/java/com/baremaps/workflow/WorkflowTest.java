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

import com.baremaps.testing.PostgresContainerTest;
import com.baremaps.workflow.tasks.DownloadUrl;
import com.baremaps.workflow.tasks.ImportGeoPackage;
import com.baremaps.workflow.tasks.ImportOpenStreetMap;
import com.baremaps.workflow.tasks.ImportShapefile;
import com.baremaps.workflow.tasks.UnzipFile;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class WorkflowTest extends PostgresContainerTest {

  @Test
  @Disabled
  void execute() {
    var workflow =
        new Workflow(
            new DownloadUrl(
                "fetch-geopackage",
                List.of(),
                "https://tiles.baremaps.com/samples/import_db.gpkg",
                "downloads/import_db.gpkg"),
            new ImportGeoPackage(
                "import-geopackage",
                List.of("fetch-geopackage"),
                "downloads/import_db.gpkg",
                getJdbcUrl(),
                4326,
                3857),
            new DownloadUrl(
                "fetch-osmpbf",
                List.of(),
                "https://tiles.baremaps.com/samples/liechtenstein.osm.pbf",
                "downloads/liechtenstein.osm.pbf"),
            new ImportOpenStreetMap(
                "import-osmpbf",
                List.of("fetch-osmpbf"),
                "downloads/liechtenstein.osm.pbf",
                getJdbcUrl(),
                3857),
            new DownloadUrl(
                "fetch-shapefile",
                List.of(),
                "https://osmdata.openstreetmap.de/download/simplified-water-polygons-split-3857.zip",
                "downloads/simplified-water-polygons-split-3857.zip"),
            new UnzipFile(
                "unzip-shapefile",
                List.of("fetch-shapefile"),
                "downloads/simplified-water-polygons-split-3857.zip",
                "archives"),
            new DownloadUrl(
                "fetch-projection",
                List.of("unzip-shapefile"),
                "https://spatialreference.org/ref/sr-org/epsg3857/prj/",
                "archives/simplified-water-polygons-split-3857/simplified_water_polygons.prj"),
            new ImportShapefile(
                "import-shapefile",
                List.of("fetch-projection"),
                "archives/simplified-water-polygons-split-3857/simplified_water_polygons.shp",
                getJdbcUrl(),
                3857,
                3857));
    new WorkflowExecutor(workflow).execute().join();
  }
}
