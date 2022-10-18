/*
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

package org.apache.baremaps.workflow;



import java.util.List;
import org.apache.baremaps.testing.PostgresContainerTest;
import org.apache.baremaps.workflow.tasks.DownloadUrl;
import org.apache.baremaps.workflow.tasks.ImportGeoPackage;
import org.apache.baremaps.workflow.tasks.ImportOpenStreetMap;
import org.apache.baremaps.workflow.tasks.ImportShapefile;
import org.apache.baremaps.workflow.tasks.UnzipFile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class WorkflowTest extends PostgresContainerTest {

  @Test
  @Disabled
  void naturalearthGeoPackage() {
    var workflow = new Workflow(List.of(new Step("fetch-geopackage", List.of(), List.of(
        new DownloadUrl("https://naciscdn.org/naturalearth/packages/natural_earth_vector.gpkg.zip",
            "natural_earth_vector.gpkg.zip"),
        new UnzipFile("natural_earth_vector.gpkg.zip", "natural_earth_vector"),
        new ImportGeoPackage("natural_earth_vector/packages/natural_earth_vector.gpkg", jdbcUrl(),
            4326, 3857)))));
    new WorkflowExecutor(workflow).execute().join();
  }

  @Test
  @Disabled
  void coastlineShapefile() {
    var workflow = new Workflow(List.of(new Step("fetch-geopackage", List.of(),
        List.of(
            new DownloadUrl("https://osmdata.openstreetmap.de/download/coastlines-split-4326.zip",
                "coastlines-split-4326.zip"),
            new UnzipFile("coastlines-split-4326.zip", "coastlines-split-4326"),
            new ImportShapefile("coastlines-split-4326/coastlines-split-4326/lines.shp", jdbcUrl(),
                4326, 3857)))));
    new WorkflowExecutor(workflow).execute().join();
  }

  @Test
  @Disabled
  void simplifiedWaterPolygonsShapefile() {
    var workflow = new Workflow(List.of(new Step("simplified-water-polygons", List.of(), List.of(
        /*
         * new DownloadUrl(
         * "https://osmdata.openstreetmap.de/download/simplified-water-polygons-split-3857.zip",
         * "simplified-water-polygons-split-3857.zip"), new UnzipFile(
         * "simplified-water-polygons-split-3857.zip", "simplified-water-polygons-split-3857"),
         */
        new ImportShapefile(
            "simplified-water-polygons-split-3857/simplified-water-polygons-split-3857/simplified_water_polygons.shp",
            "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps", 3857,
            3857)))));
    new WorkflowExecutor(workflow).execute().join();
  }

  @Test
  @Disabled
  void workflow() {
    var workflow = new Workflow(List.of(new Step("fetch-geopackage", List.of(), List.of(
        new DownloadUrl("https://naciscdn.org/naturalearth/packages/natural_earth_vector.gpkg.zip",
            "downloads/import_db.gpkg"),
        new ImportShapefile("downloads/import_db.gpkg", jdbcUrl(), 4326, 3857)))));
    new WorkflowExecutor(workflow).execute().join();
  }

  @Test
  @Disabled
  void execute() {
    var workflow = new Workflow(List.of(
        new Step("fetch-geopackage", List.of(),
            List.of(new DownloadUrl("https://tiles.baremaps.com/samples/import_db.gpkg",
                "downloads/import_db.gpkg"))),
        new Step("import-geopackage", List.of("fetch-geopackage"),
            List.of(new ImportGeoPackage("downloads/import_db.gpkg", jdbcUrl(), 4326, 3857))),
        new Step("fetch-osmpbf", List.of(),
            List.of(new DownloadUrl("https://tiles.baremaps.com/samples/liechtenstein.osm.pbf",
                "downloads/liechtenstein.osm.pbf"))),
        new Step("import-osmpbf", List.of("fetch-osmpbf"),
            List.of(new ImportOpenStreetMap("downloads/liechtenstein.osm.pbf", jdbcUrl(), 3857))),
        new Step("fetch-shapefile", List.of(), List.of(new DownloadUrl(
            "https://osmdata.openstreetmap.de/download/simplified-water-polygons-split-3857.zip",
            "downloads/simplified-water-polygons-split-3857.zip"))),
        new Step("unzip-shapefile", List.of("fetch-shapefile"),
            List.of(
                new UnzipFile("downloads/simplified-water-polygons-split-3857.zip", "archives"))),
        new Step("fetch-projection", List.of("unzip-shapefile"),
            List.of(new DownloadUrl("https://spatialreference.org/ref/sr-org/epsg3857/prj/",
                "archives/simplified-water-polygons-split-3857/simplified_water_polygons.prj"))),
        new Step("import-shapefile", List.of("fetch-projection"),
            List.of(new ImportShapefile(
                "archives/simplified-water-polygons-split-3857/simplified_water_polygons.shp",
                jdbcUrl(), 3857, 3857)))));
    new WorkflowExecutor(workflow).execute().join();
  }
}
