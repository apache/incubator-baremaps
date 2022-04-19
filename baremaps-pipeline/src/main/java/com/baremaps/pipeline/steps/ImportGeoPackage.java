package com.baremaps.pipeline.steps;

import com.baremaps.pipeline.Context;
import com.baremaps.pipeline.PipelineException;
import com.baremaps.storage.geopackage.GeoPackageStore;
import java.util.List;
import mil.nga.geopackage.GeoPackageManager;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.Resource;

public record ImportGeoPackage(String id, List<String> needs, String file) implements ImportFeatureStep {

  @Override
  public void execute(Context context) {
    try {
      var path = context.directory().resolve(file);
      var geoPackageStore = new GeoPackageStore(GeoPackageManager.open(path.toFile()));
      for (Resource resource : geoPackageStore.components()) {
        if (resource instanceof FeatureSet featureSet) {
          saveFeatureSet(context, featureSet);
        }
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }
}
