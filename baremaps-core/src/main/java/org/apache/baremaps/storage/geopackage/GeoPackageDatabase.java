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

package org.apache.baremaps.storage.geopackage;



import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import org.apache.baremaps.feature.ReadableAggregate;
import org.apache.baremaps.feature.Resource;


public class GeoPackageDatabase implements ReadableAggregate, AutoCloseable {

  private final GeoPackage geoPackage;

  public GeoPackageDatabase(Path path) {
    this.geoPackage = GeoPackageManager.open(path.toFile());
  }

  @Override
  public Stream<Resource> read() throws IOException {
    return geoPackage.getFeatureTables().stream()
        .map(table -> new GeoPackageTable(geoPackage.getFeatureDao(table)));
  }

  @Override
  public void close() throws Exception {
    geoPackage.close();
  }
}
