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

  default void saveFeatureSet(Context context, FeatureSet featureSet) {
    try  {
      var dataStore = context.postgresStore();
      var type = featureSet.getType();
      var typeName = type.getName().toString();
      try {
        dataStore.deleteFeatureType(typeName);
      } catch (Exception e) {
        // do nothing
      }
      dataStore.createFeatureType(type);
      var target = dataStore.findResource(type.getName().toString());
      if (target instanceof WritableFeatureSet writableFeatureSet) {
        try (var featureStream = featureSet.features(false)) {
          writableFeatureSet.add(featureStream.map(feature -> reprojectFeature(context, feature)).iterator());
        }
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  default Feature reprojectFeature(Context context, Feature feature) {
    for (var property : feature.getType().getProperties(false)) {
      String name = property.getName().toString();
      if (feature.getPropertyValue(name) instanceof Geometry geometry) {
        Geometry value = new ProjectionTransformer(geometry.getSRID(), context.srid())
            .transform(geometry);
        feature.setPropertyValue(name, value);
      }
    }
    return feature;
  }

}
