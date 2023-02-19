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



import java.io.IOException;
import java.util.stream.Stream;
import org.apache.baremaps.feature.Feature;
import org.apache.baremaps.feature.FeatureType;
import org.apache.baremaps.feature.ReadableFeatureSet;
import org.apache.baremaps.openstreetmap.utils.ProjectionTransformer;
import org.locationtech.jts.geom.Geometry;

public class FeatureSetProjectionTransform implements ReadableFeatureSet {

  private final ReadableFeatureSet featureSetReader;

  private final ProjectionTransformer projectionTransformer;

  public FeatureSetProjectionTransform(ReadableFeatureSet featureSetReader,
      ProjectionTransformer projectionTransformer) {
    this.featureSetReader = featureSetReader;
    this.projectionTransformer = projectionTransformer;
  }

  @Override
  public FeatureType getType() throws IOException {
    return featureSetReader.getType();
  }

  @Override
  public Stream<Feature> read() throws IOException {
    return featureSetReader.read().map(this::transformProjection);
  }

  public Feature transformProjection(Feature feature) {
    for (var property : feature.getType().getProperties().values()) {
      var name = property.getName();
      var value = feature.getProperty(name);
      if (value instanceof Geometry geometry) {
        var projectedGeometry = projectionTransformer.transform(geometry);
        feature.setProperty(name, projectedGeometry);
      }
    }
    return feature;
  }
}
