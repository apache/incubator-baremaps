package org.apache.sis.storage.geopackage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import mil.nga.geopackage.GeoPackage;
import org.apache.sis.storage.Aggregate;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.Resource;
import org.apache.sis.storage.sql.SQLStore;
import org.opengis.metadata.Metadata;
import org.opengis.parameter.ParameterValueGroup;

public class GeoPackageStore extends DataStore implements Aggregate {

  private final GeoPackage geoPackage;

  public GeoPackageStore(GeoPackage geoPackage) {
    this.geoPackage = geoPackage;
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
  public Collection<? extends Resource> components() throws DataStoreException {
    return geoPackage.getFeatureTables().stream()
        .map(table -> new GeoPackageTableStore(geoPackage.getFeatureDao(table)))
        .collect(Collectors.toList());
  }

}
