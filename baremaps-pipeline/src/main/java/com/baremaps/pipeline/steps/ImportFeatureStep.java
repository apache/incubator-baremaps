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

package com.baremaps.pipeline.steps;

import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.pipeline.Context;
import com.baremaps.pipeline.PipelineException;
import com.baremaps.pipeline.Step;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.WritableFeatureSet;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Feature;

public interface ImportFeatureStep extends Step {

  Integer sourceSRID();

  Integer targetSRID();

  default void saveFeatureSet(Context context, FeatureSet sourceFeatureSet) {
    try {
      var type = sourceFeatureSet.getType();
      var typeName = type.getName().toString();

      // clean the target store
      var targetDataStore = context.postgresStore();
      try {
        targetDataStore.deleteFeatureType(typeName);
      } catch (Exception e) {
        // do nothing
      }
      targetDataStore.createFeatureType(type);

      // transfer the features from the source to the target store
      var targetResource = targetDataStore.findResource(typeName);
      if (targetResource instanceof WritableFeatureSet targetFeatureSet) {
        try (var sourceFeatureStream = sourceFeatureSet.features(false)) {
          if (sourceSRID().equals(targetSRID())) {
            targetFeatureSet.add(sourceFeatureStream.iterator());
          } else {
            targetFeatureSet.add(
                sourceFeatureStream.map(feature -> reprojectFeature(context, feature)).iterator());
          }
        }
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  default Feature reprojectFeature(Context context, Feature feature) {
    for (var property : feature.getType().getProperties(false)) {
      String name = property.getName().toString();
      if (feature.getPropertyValue(name) instanceof Geometry inputGeometry) {
        Geometry outputGeometry =
            new ProjectionTransformer(sourceSRID(), targetSRID()).transform(inputGeometry);
        feature.setPropertyValue(name, outputGeometry);
      }
    }
    return feature;
  }
}
