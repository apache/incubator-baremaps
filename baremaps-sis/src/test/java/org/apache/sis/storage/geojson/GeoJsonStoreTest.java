package org.apache.sis.storage.geojson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import mil.nga.sf.Geometry;
import org.apache.sis.feature.AbstractFeature;
import org.apache.sis.feature.DefaultAttributeType;
import org.apache.sis.feature.DefaultFeatureType;
import org.apache.sis.storage.DataStoreException;
import org.junit.jupiter.api.Test;

class GeoJsonStoreTest {

   String geojson() throws IOException {
    return Resources.toString(Resources.getResource("geojson.json"), Charsets.UTF_8);
  }

  @Test
  void getType() throws IOException, DataStoreException {
    GeoJsonStore store = new GeoJsonStore(geojson());
    DefaultFeatureType type = store.getType();
    assertEquals(String.class, ((DefaultAttributeType) type.getProperty("string")).getValueClass());
    assertEquals(Integer.class, ((DefaultAttributeType) type.getProperty("integer")).getValueClass());
    assertEquals(Geometry.class, ((DefaultAttributeType) type.getProperty("geometry")).getValueClass());
  }

  @Test
  void features() throws IOException, DataStoreException {
    GeoJsonStore store = new GeoJsonStore(geojson());
    List<AbstractFeature> features = store.features(false).collect(Collectors.toList());
    assertEquals(0, features.get(0).getPropertyValue("integer"));
    assertEquals(1, features.get(1).getPropertyValue("integer"));
  }
}