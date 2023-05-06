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

/**
 * A store corresponding to a GeoPackage database.
 */
public class GeoPackageStore implements Store, AutoCloseable {

  private final GeoPackage geoPackage;

  /**
   * Constructs a store from a GeoPackage database.
   *
   * @param file the path to the GeoPackage database
   */
  public GeoPackageStore(Path file) {
    this.geoPackage = GeoPackageManager.open(file.toFile());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws Exception {
    geoPackage.close();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<String> list() throws TableException {
    return geoPackage.getFeatureTables();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Table get(String name) throws TableException {
    return new GeoPackageTable(geoPackage.getFeatureDao(name));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(Table value) throws TableException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(String name) throws TableException {
    throw new UnsupportedOperationException();
  }
}
