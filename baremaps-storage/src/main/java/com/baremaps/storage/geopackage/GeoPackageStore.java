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

package com.baremaps.storage.geopackage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import mil.nga.geopackage.GeoPackage;
import org.apache.sis.storage.Aggregate;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.Resource;
import org.opengis.metadata.Metadata;
import org.opengis.parameter.ParameterValueGroup;

public class GeoPackageStore extends DataStore implements Aggregate {

  private final GeoPackage geoPackage;

  public GeoPackageStore(GeoPackage geoPackage) {
    this.geoPackage = geoPackage;
  }

  @Override
  public Optional<ParameterValueGroup> getOpenParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Metadata getMetadata() throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws DataStoreException {}

  @Override
  public Collection<? extends Resource> components() throws DataStoreException {
    return geoPackage.getFeatureTables().stream()
        .map(table -> new GeoPackageTableStore(geoPackage.getFeatureDao(table)))
        .collect(Collectors.toList());
  }
}
