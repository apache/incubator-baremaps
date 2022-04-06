package org.apache.sis.storage.geopackage;

import java.io.File;
import mil.nga.geopackage.GeoPackageManager;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.junit.jupiter.api.Test;

class GeoPackageStoreTest {

  @Test
  void components() throws DataStoreException {
    GeoPackageStore geoPackageStore = new GeoPackageStore(
        GeoPackageManager.open(new File("src/test/resources/geopackage.gpkg")));
    geoPackageStore.components().forEach(resource -> {
      try {
        FeatureSet featureSet = (FeatureSet) resource;
        System.out.println(featureSet.getType());
        featureSet.features(false).forEach(abstractFeature -> {
          System.out.println(abstractFeature);
        });
      } catch (DataStoreException e) {
        e.printStackTrace();
      }
    });

  }
}