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
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.WritableFeatureSet;
import org.geotoolkit.db.postgres.PostgresStore;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.AttributeType;
import org.opengis.feature.Feature;

public interface ImportFeatureTask extends Task {

  String database();

  Integer sourceSRID();

  Integer targetSRID();

  default void saveFeatureSet(FeatureSet sourceFeatureSet) {
    try {
      // Extract the connection parameters from the jdbc url.
      var uri = URI.create(database().substring(5));
      var host = uri.getHost();
      var port = uri.getPort();
      var database = uri.getPath().substring(1);
      var params =
          Arrays.stream(uri.getQuery().split("&"))
              .map(param -> param.split("=", 2))
              .collect(Collectors.toMap(param -> param[0], param -> param[1]));
      var user = params.get("user");
      var password = params.get("password");
      var schema = params.getOrDefault("currentSchema", "public");

      // Create a postgres feature store.
      try (var targetDataStore = new PostgresStore(host, port, database, schema, user, password)) {
        var type = sourceFeatureSet.getType();
        var typeName = type.getName().toString();

        // Try to clean the target store.
        try {
          targetDataStore.deleteFeatureType(typeName);
        } catch (Exception e) {
          // Fail silently as there is no feature type to delete.
        }
        targetDataStore.createFeatureType(type);

        // Transfer the features from the source to the target store.
        var targetResource = targetDataStore.findResource(typeName);
        if (targetResource instanceof WritableFeatureSet targetFeatureSet) {
          try (var sourceFeatureStream = sourceFeatureSet.features(false)) {
            if (sourceSRID().equals(targetSRID())) {
              targetFeatureSet.add(sourceFeatureStream.iterator());
            } else {
              var reprojectedFeatures = sourceFeatureStream.map(feature -> reprojectFeature(feature));
              targetFeatureSet.add(reprojectedFeatures.iterator());
            }
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
      if (property instanceof AttributeType
          && feature.getPropertyValue(name) instanceof Geometry inputGeometry) {
        Geometry outputGeometry =
            new ProjectionTransformer(sourceSRID(), targetSRID()).transform(inputGeometry);
        feature.setPropertyValue(name, outputGeometry);
      }
    }
    return feature;
  }
}
