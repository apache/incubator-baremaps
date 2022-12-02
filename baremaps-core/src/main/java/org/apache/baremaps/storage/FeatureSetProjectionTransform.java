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

package org.apache.baremaps.storage;



import java.util.Optional;
import java.util.stream.Stream;
import org.apache.baremaps.openstreetmap.utils.ProjectionTransformer;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.event.StoreEvent;
import org.apache.sis.storage.event.StoreListener;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.Metadata;
import org.opengis.util.GenericName;

public class FeatureSetProjectionTransform implements FeatureSet {

  private final FeatureSet featureSet;

  private final ProjectionTransformer projectionTransformer;

  public FeatureSetProjectionTransform(FeatureSet featureSet,
      ProjectionTransformer projectionTransformer) {
    this.featureSet = featureSet;
    this.projectionTransformer = projectionTransformer;
  }

  @Override
  public FeatureType getType() throws DataStoreException {
    return featureSet.getType();
  }

  @Override
  public Stream<Feature> features(boolean parallel) throws DataStoreException {
    return featureSet.features(parallel).map(this::transformProjection);
  }

  public Feature transformProjection(Feature feature) {
    for (var property : feature.getType().getProperties(true)) {
      var name = property.getName().toString();
      var value = feature.getPropertyValue(name);
      if (value instanceof Geometry geometry) {
        var projectedGeometry = projectionTransformer.transform(geometry);
        feature.setPropertyValue(name, projectedGeometry);
      }
    }
    return feature;
  }

  @Override
  public Optional<Envelope> getEnvelope() throws DataStoreException {
    return featureSet.getEnvelope();
  }

  @Override
  public Optional<GenericName> getIdentifier() throws DataStoreException {
    return featureSet.getIdentifier();
  }

  @Override
  public Metadata getMetadata() throws DataStoreException {
    return featureSet.getMetadata();
  }

  @Override
  public <T extends StoreEvent> void addListener(Class<T> eventType,
      StoreListener<? super T> listener) {
    featureSet.addListener(eventType, listener);
  }

  @Override
  public <T extends StoreEvent> void removeListener(Class<T> eventType,
      StoreListener<? super T> listener) {
    featureSet.removeListener(eventType, listener);
  }
}
