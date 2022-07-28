package com.baremaps.storage.postgres;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.Resource;
import org.apache.sis.storage.WritableAggregate;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.AttributeType;
import org.opengis.feature.FeatureType;
import org.opengis.metadata.Metadata;
import org.opengis.parameter.ParameterValueGroup;

public class PostgresStore extends DataStore implements WritableAggregate {

  private final DataSource dataSource;

  public PostgresStore(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Collection<? extends Resource> components() throws DataStoreException {
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
  public Resource add(Resource resource) throws DataStoreException {
    if (resource instanceof FeatureSet featureSet) {
      var type = featureSet.getType();
      var features = featureSet.features(false);
      try (var connection = dataSource.getConnection()) {
        String statement = createTable(type);
        return null;
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new DataStoreException("Unsupported resource type");
    }
  }

  @Override
  public void remove(Resource resource) throws DataStoreException {
    if (resource instanceof FeatureSet featureSet) {
      var type = featureSet.getType();
      var features = featureSet.features(false);
      try (var connection = dataSource.getConnection()) {
        connection.createStatement().executeQuery(String.format("DROP TABLE IF EXISTS %s", type.getName()));
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new DataStoreException("Unsupported resource type");
    }
  }

  private String createTable(FeatureType type) {
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE ");
    builder.append(type.getName());
    for (var property : type.getProperties(false)) {
      if (property instanceof AttributeType attribute) {
        attribute.getName();
        attribute.getValueClass();
      }
    }
    return builder.toString();
  }

  private String dropTable(FeatureType type) {
    return String.format("DROP TABLE IF EXISTS %s", type.getName());
  }

}
