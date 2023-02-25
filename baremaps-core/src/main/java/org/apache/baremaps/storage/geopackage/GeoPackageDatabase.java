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
import org.apache.baremaps.dataframe.DataFrame;
import org.apache.baremaps.dataframe.DataFrameException;
import org.apache.baremaps.dataframe.DataStore;


public class GeoPackageDatabase implements DataStore, AutoCloseable {

  private final GeoPackage geoPackage;

  public GeoPackageDatabase(Path path) {
    this.geoPackage = GeoPackageManager.open(path.toFile());
  }

  @Override
  public void close() throws Exception {
    geoPackage.close();
  }

  @Override
  public Collection<DataFrame> list() throws DataFrameException {
    return geoPackage.getFeatureTables().stream()
        .map(table -> new GeoPackageTable(geoPackage.getFeatureDao(table)))
        .map(DataFrame.class::cast)
        .toList();
  }

  @Override
  public DataFrame get(String name) throws DataFrameException {
    return list().stream()
        .filter(dataFrame -> dataFrame.dataType().name().equals(name))
        .findFirst()
        .orElseThrow(() -> new DataFrameException());
  }

  @Override
  public void add(DataFrame value) throws DataFrameException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void remove(String name) throws DataFrameException {
    throw new UnsupportedOperationException();
  }
}
