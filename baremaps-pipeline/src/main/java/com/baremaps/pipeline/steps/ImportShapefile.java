package com.baremaps.pipeline.steps;

import com.baremaps.pipeline.Context;
import com.baremaps.pipeline.PipelineException;
import java.net.URI;
import java.util.List;
import org.apache.sis.storage.FeatureSet;
import org.geotoolkit.data.shapefile.ShapefileFeatureStore;

public record ImportShapefile(String id, List<String> needs, String file) implements ImportFeatureStep {

  @Override
  public void execute(Context context) {
    try {
      URI uri = context.directory().resolve(file).toUri();
      var shapefileStore = new ShapefileFeatureStore(uri);
      for (var resource : shapefileStore.components()) {
        if (resource instanceof FeatureSet featureSet) {
          saveFeatureSet(context, featureSet);
        }
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }
}
