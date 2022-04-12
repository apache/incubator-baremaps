package com.baremaps.storage.geopackage;

import com.google.common.io.Resources;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import mil.nga.geopackage.GeoPackageManager;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.Resource;
import org.junit.jupiter.api.Test;

class GeoPackageStoreTest {

  @Test
  void components() throws URISyntaxException, DataStoreException {
    URI uri = Resources.getResource("data.gpkg").toURI();
    var store = new GeoPackageStore(GeoPackageManager.open(new File(uri)));
    for (Resource resource :store.components()) {
      if (resource instanceof FeatureSet featureSet) {
        featureSet.features(false).forEach(System.out::println);
      }
    }
  }
}