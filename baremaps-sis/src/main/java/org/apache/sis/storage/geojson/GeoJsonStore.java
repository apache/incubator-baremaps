package org.apache.sis.storage.geojson;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import mil.nga.sf.Geometry;
import mil.nga.sf.geojson.Feature;
import mil.nga.sf.geojson.FeatureCollection;
import mil.nga.sf.geojson.FeatureConverter;
import org.apache.sis.feature.AbstractFeature;
import org.apache.sis.feature.DefaultFeatureType;
import org.apache.sis.feature.builder.FeatureTypeBuilder;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.Metadata;
import org.opengis.parameter.ParameterValueGroup;

public class GeoJsonStore extends DataStore implements FeatureSet {

  private final FeatureCollection featureCollection;

  private final DefaultFeatureType featureType;

  public GeoJsonStore(String json) {
    this("GeoJSON", json);
  }

  public GeoJsonStore(String name, String json) {
    featureCollection = FeatureConverter.toFeatureCollection(json);
    Set<String> propertyNames = new HashSet<>();
    FeatureTypeBuilder builder = new FeatureTypeBuilder().setName(name);
    for (Feature feature : featureCollection.getFeatures()) {
      Map<String, Object> properties = feature.getProperties();
      for (final String propertyName : properties.keySet()) {
        if (!propertyNames.contains(propertyName)) {
          Class<?> propertyType = properties.get(propertyName).getClass();
          builder.addAttribute(propertyType).setName(propertyName);
          propertyNames.add(propertyName);
        }
      }
    }
    builder.addAttribute(Geometry.class).setName("geometry");
    featureType = builder.build();
  }

  @Override
  public Optional<Envelope> getEnvelope() throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<ParameterValueGroup> getOpenParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Metadata getMetadata() throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws DataStoreException {

  }

  @Override
  public DefaultFeatureType getType() throws DataStoreException {
    return featureType;
  }

  @Override
  public Stream<AbstractFeature> features(boolean parallel) throws DataStoreException {
    return featureCollection.getFeatures().stream().map(this::asAbstractFeature);
  }

  private AbstractFeature asAbstractFeature(Feature feature) {
    AbstractFeature abstractFeature = featureType.newInstance();
    for (Entry<String, Object> property : feature.getProperties().entrySet()) {
      abstractFeature.setPropertyValue(property.getKey(), property.getValue());
    }
    abstractFeature.setPropertyValue("geometry", feature.getGeometry().getGeometry());
    return abstractFeature;
  }
}
