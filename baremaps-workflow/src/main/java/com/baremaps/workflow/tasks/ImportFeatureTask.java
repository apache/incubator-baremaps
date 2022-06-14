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

package com.baremaps.workflow.tasks;

import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.workflow.Task;
import com.baremaps.workflow.WorkflowException;
import com.baremaps.workflow.model.Database;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.WritableFeatureSet;
import org.geotoolkit.db.postgres.PostgresStore;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Feature;

public interface ImportFeatureTask extends Task {

  Database database();

  Integer sourceSRID();

  Integer targetSRID();

  default void saveFeatureSet(FeatureSet sourceFeatureSet) {
    try {
      var type = sourceFeatureSet.getType();
      var typeName = type.getName().toString();

      var targetDataStore =
          new PostgresStore(
              database().host(),
              database().port(),
              database().name(),
              database().schema(),
              database().username(),
              database().password());

      // clean the target store
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
                sourceFeatureStream.map(feature -> reprojectFeature(feature)).iterator());
          }
        }
      }
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }

  default Feature reprojectFeature(Feature feature) {
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
