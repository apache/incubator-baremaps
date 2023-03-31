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


import java.nio.file.Path;
import java.util.Collection;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import org.apache.baremaps.storage.Store;
import org.apache.baremaps.storage.Table;
import org.apache.baremaps.storage.TableException;


public class GeoPackageStore implements Store, AutoCloseable {

  private final GeoPackage geoPackage;

  public GeoPackageStore(Path path) {
    this.geoPackage = GeoPackageManager.open(path.toFile());
  }

  @Override
  public void close() throws Exception {
    geoPackage.close();
  }

  @Override
  public Collection<String> list() throws TableException {
    return geoPackage.getFeatureTables();
  }

  @Override
  public Table get(String name) throws TableException {
    return new GeoPackageTable(geoPackage.getFeatureDao(name));
  }

  @Override
  public void add(Table value) throws TableException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void remove(String name) throws TableException {
    throw new UnsupportedOperationException();
  }
}
