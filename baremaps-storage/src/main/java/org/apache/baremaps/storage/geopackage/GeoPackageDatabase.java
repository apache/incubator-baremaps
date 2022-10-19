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
import java.util.Optional;
import java.util.stream.Collectors;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import org.apache.sis.storage.Aggregate;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.Resource;
import org.apache.sis.storage.event.StoreEvent;
import org.apache.sis.storage.event.StoreListener;
import org.opengis.metadata.Metadata;
import org.opengis.util.GenericName;

public class GeoPackageDatabase implements Aggregate, AutoCloseable {

  private final GeoPackage geoPackage;

  public GeoPackageDatabase(Path path) {
    this.geoPackage = GeoPackageManager.open(path.toFile());
  }

  @Override
  public Optional<GenericName> getIdentifier() throws DataStoreException {
    return Optional.empty();
  }

  @Override
  public Metadata getMetadata() throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends StoreEvent> void addListener(Class<T> eventType,
      StoreListener<? super T> listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends StoreEvent> void removeListener(Class<T> eventType,
      StoreListener<? super T> listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<? extends Resource> components() throws DataStoreException {
    return geoPackage.getFeatureTables().stream()
        .map(table -> new GeoPackageTable(geoPackage.getFeatureDao(table)))
        .collect(Collectors.toList());
  }

  @Override
  public void close() throws Exception {
    geoPackage.close();
  }
}
