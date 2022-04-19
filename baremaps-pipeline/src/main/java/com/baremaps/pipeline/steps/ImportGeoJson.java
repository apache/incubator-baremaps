package com.baremaps.pipeline.steps;

import com.baremaps.pipeline.Context;
import com.baremaps.pipeline.PipelineException;
import java.util.List;
import org.apache.sis.storage.DataStores;
import org.apache.sis.storage.FeatureSet;

public record ImportGeoJson(String id, List<String> needs, String file) implements ImportFeatureStep {

  @Override
  public void execute(Context context) {
    try {
      var uri = context.directory().resolve(file).toUri();
      var featureSet = (FeatureSet) DataStores.open(uri);
      saveFeatureSet(context, featureSet);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }
}
