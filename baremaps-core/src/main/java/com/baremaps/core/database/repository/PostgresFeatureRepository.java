package com.baremaps.core.database.repository;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Operator;
import com.esri.core.geometry.OperatorExportToWkb;
import com.esri.core.geometry.OperatorFactoryLocal;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.sis.feature.AbstractFeature;
import org.apache.sis.feature.AbstractIdentifiedType;
import org.apache.sis.feature.DefaultFeatureType;
import org.apache.sis.internal.shapefile.jdbc.DBase3FieldDescriptor;
import org.apache.sis.internal.shapefile.jdbc.DBaseDataType;

public class PostgresFeatureRepository {

  private final DefaultFeatureType featureType;

  private final Map<String, DBaseDataType> descriptors;

  private final DataSource dataSource;

  private String create;

  private String select;

  private String insert;

  private String drop;

  public PostgresFeatureRepository(
      DataSource dataSource,
      DefaultFeatureType featureType,
      Collection<DBase3FieldDescriptor> descriptors) {
    this.dataSource = dataSource;
    this.featureType = featureType;
    this.descriptors = descriptors.stream().collect(Collectors.toMap(d -> d.getName(), d -> d.getType()));
    this.create = String.format("CREATE TABLE IF NOT EXISTS %s (%s)",
        tableName(),
        featureType.getProperties(false).stream()
            .map(this::columnDefinition)
            .collect(Collectors.joining(", ")));
    this.select = String.format("SELECT * FROM %s", tableName());
    this.insert = String.format("INSERT INTO %s (%s) VALUES (%s)",
        tableName(),
        featureType.getProperties(false).stream()
            .map(a -> a.getName().toString())
            .collect(Collectors.joining(", ")),
        featureType.getProperties(false).stream()
            .map(a -> "?").collect(Collectors.joining(", ")));
    this.drop = String.format("DROP TABLE IF EXISTS %s CASCADE", tableName());
  }



  public void create() throws RepositoryException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(create)) {
      statement.execute();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  public void drop() throws RepositoryException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(drop)) {
      statement.execute();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  public void insert(AbstractFeature feature) throws RepositoryException {
    if (feature == null) {
      return;
    }
    if (!feature.getType().equals(featureType)) {
      throw new IllegalArgumentException("The type of the feature is not compatible with this store.");
    }
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      int i = 1;
      for (AbstractIdentifiedType type : featureType.getProperties(false)) {
        String name = type.getName().toString();
        Object value = feature.getPropertyValue(name);
        if (value instanceof Geometry) {
          OperatorExportToWkb operatorExport = (OperatorExportToWkb) OperatorFactoryLocal
              .getInstance().getOperator(Operator.Type.ExportToWkb);
          ByteBuffer byteBuffer = operatorExport.execute(0, (Geometry) value, null);
          value = byteBuffer.array();
        } else {
          value = valueType(descriptors.get(name), (String) value);
        }
        statement.setObject(i++, value);
      }
      statement.execute();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }

  }

  private String tableName() {
    return featureType.getName().toString().replace(".", "_");
  }

  private String columnDefinition(AbstractIdentifiedType attributeType) {
    String name = attributeType.getName().toString();
    String type = Optional.ofNullable(descriptors.get(name))
        .map(this::columnType)
        .orElse("geometry");
    return String.format("%s %s", name, type);
  }

  private String columnType(DBaseDataType type) {
    switch (type) {
      case Integer:
      case AutoIncrement:
        return "integer";
      case Number:
      case Double:
      case FloatingPoint:
        return "numeric";
      case Picture:
      case Character:
      case Logical:
      case Date:
      case Memo:
      case Currency:
      case DateTime:
      case VariField:
      case Variant:
      case TimeStamp:
      default:
        return "varchar";
    }
  }

  private Object valueType(DBaseDataType type, String value) {
    switch (type) {
      case Integer:
      case AutoIncrement:
        return Integer.valueOf(value);
      case Number:
      case Double:
      case FloatingPoint:
        return Double.valueOf(value);
      case Picture:
      case Character:
      case Logical:
      case Date:
      case Memo:
      case Currency:
      case DateTime:
      case VariField:
      case Variant:
      case TimeStamp:
      default:
        return value;
    }
  }

}
