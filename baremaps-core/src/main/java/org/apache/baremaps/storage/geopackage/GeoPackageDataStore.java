/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.storage.geopackage;


import java.nio.file.Path;
import java.util.List;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import org.apache.baremaps.data.schema.DataFrame;
import org.apache.baremaps.data.schema.DataStore;
import org.apache.baremaps.data.schema.DataStoreException;

/**
 * A schema corresponding to a GeoPackage database.
 */
public class GeoPackageDataStore implements DataStore, AutoCloseable {

  private final GeoPackage geoPackage;

  /**
   * Constructs a schema from a GeoPackage database.
   *
   * @param file the path to the GeoPackage database
   */
  public GeoPackageDataStore(Path file) {
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
  public List<String> list() throws DataStoreException {
    return geoPackage.getFeatureTables();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataFrame get(String name) throws DataStoreException {
    return new GeoPackageDataFrame(geoPackage.getFeatureDao(name));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(DataFrame frame) throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(String name, DataFrame frame) throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(String name) throws DataStoreException {
    throw new UnsupportedOperationException();
  }
}
