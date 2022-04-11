package com.baremaps.storage.geopackage;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import org.apache.sis.feature.builder.FeatureTypeBuilder;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.Metadata;
import org.opengis.parameter.ParameterValueGroup;

public class GeoPackageTableStore extends DataStore implements FeatureSet {

  private final FeatureDao featureDao;

  private final FeatureType featureType;

  public GeoPackageTableStore(FeatureDao featureDao) {
    this.featureDao = featureDao;
    FeatureTypeBuilder builder = new FeatureTypeBuilder().setName(featureDao.getTableName());
    for (FeatureColumn column : featureDao.getColumns()) {
      builder.addAttribute(column.getDataType().getClassType()).setName(column.getName());
    }
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
  public FeatureType getType() throws DataStoreException {
    return featureType;
  }

  @Override
  public Stream<Feature> features(boolean parallel) throws DataStoreException {
    Iterator<Feature> featureIterator = new FeatureIterator(featureDao.queryForAll(), featureType);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(featureIterator, 0), false);
  }

  public class FeatureIterator implements Iterator<Feature> {

    private final FeatureResultSet featureResultSet;

    private final FeatureType featureType;

    public FeatureIterator(FeatureResultSet featureResultSet, FeatureType featureType) {
      this.featureResultSet = featureResultSet;
      this.featureType = featureType;
    }

    @Override
    public boolean hasNext() {
      return featureResultSet.getPosition() <= featureResultSet.getCount();
    }

    @Override
    public Feature next() {
      if (featureResultSet.getPosition() > featureResultSet.getCount()) {
        throw new NoSuchElementException();
      }
      Feature feature = featureType.newInstance();
      for (FeatureColumn featureColumn: featureResultSet.getColumns().getColumns()) {
        System.out.println(featureColumn.getName());
        System.out.println(featureColumn.getType());
        feature.setPropertyValue(featureColumn.getName(), featureResultSet.getValue(featureColumn));
      }
      return feature;
    }
  }

}
