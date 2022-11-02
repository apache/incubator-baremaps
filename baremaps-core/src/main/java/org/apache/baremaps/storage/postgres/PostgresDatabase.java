/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.storage.postgres;



import de.bytefish.pgbulkinsert.pgsql.handlers.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.baremaps.database.copy.CopyWriter;
import org.apache.baremaps.database.copy.PostgisGeometryValueHandler;
import org.apache.sis.feature.builder.FeatureTypeBuilder;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.Resource;
import org.apache.sis.storage.WritableAggregate;
import org.apache.sis.storage.event.StoreEvent;
import org.apache.sis.storage.event.StoreListener;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.AttributeType;
import org.opengis.feature.FeatureType;
import org.opengis.feature.PropertyType;
import org.opengis.metadata.Metadata;
import org.opengis.util.GenericName;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class PostgresDatabase implements WritableAggregate {

  private static Map<Class, String> typeToName = new HashMap<>();

  static {
    typeToName.put(String.class, "varchar");
    typeToName.put(Short.class, "int2");
    typeToName.put(Integer.class, "int4");
    typeToName.put(Long.class, "int8");
    typeToName.put(Float.class, "float4");
    typeToName.put(Double.class, "float8");
    typeToName.put(Geometry.class, "geometry");
    typeToName.put(Point.class, "geometry");
    typeToName.put(MultiPoint.class, "geometry");
    typeToName.put(Point.class, "geometry");
    typeToName.put(LineString.class, "geometry");
    typeToName.put(MultiLineString.class, "geometry");
    typeToName.put(Polygon.class, "geometry");
    typeToName.put(MultiPolygon.class, "geometry");
    typeToName.put(LinearRing.class, "geometry");
    typeToName.put(GeometryCollection.class, "geometry");
    typeToName.put(LocalDate.class, "date");
    typeToName.put(LocalTime.class, "time");
    typeToName.put(LocalDateTime.class, "timestamp");
  }

  private static Map<Class, BaseValueHandler> typeToHandler = new HashMap<>();

  static {
    typeToHandler.put(String.class, new StringValueHandler());
    typeToHandler.put(Short.class, new ShortValueHandler());
    typeToHandler.put(Integer.class, new IntegerValueHandler());
    typeToHandler.put(Long.class, new LongValueHandler());
    typeToHandler.put(Float.class, new FloatValueHandler());
    typeToHandler.put(Double.class, new DoubleValueHandler());
    typeToHandler.put(Geometry.class, new PostgisGeometryValueHandler());
    typeToHandler.put(Point.class, new PostgisGeometryValueHandler());
    typeToHandler.put(MultiPoint.class, new PostgisGeometryValueHandler());
    typeToHandler.put(Point.class, new PostgisGeometryValueHandler());
    typeToHandler.put(LineString.class, new PostgisGeometryValueHandler());
    typeToHandler.put(MultiLineString.class, new PostgisGeometryValueHandler());
    typeToHandler.put(Polygon.class, new PostgisGeometryValueHandler());
    typeToHandler.put(MultiPolygon.class, new PostgisGeometryValueHandler());
    typeToHandler.put(LinearRing.class, new PostgisGeometryValueHandler());
    typeToHandler.put(GeometryCollection.class, new PostgisGeometryValueHandler());
    typeToHandler.put(LocalDate.class, new LocalDateValueHandler());
    typeToHandler.put(LocalTime.class, new LocalTimeValueHandler());
    typeToHandler.put(LocalDateTime.class, new LocalDateTimeValueHandler());
  }

  private final DataSource dataSource;

  public PostgresDatabase(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Collection<? extends Resource> components() throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<GenericName> getIdentifier() throws DataStoreException {
    return Optional.empty();
  }

  @Override
  public Metadata getMetadata() throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends StoreEvent> void addListener(Class<T> eventType,
      StoreListener<? super T> listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends StoreEvent> void removeListener(Class<T> eventType,
      StoreListener<? super T> listener) {
    throw new UnsupportedOperationException();
  }

  public FeatureType createFeatureType(FeatureType featureType) {
    var featureTypeBuilder = new FeatureTypeBuilder();
    featureTypeBuilder.setName(featureType.getName().toString().replaceAll("[^a-zA-Z0-9]", "_"));
    for (var attribute : featureType.getProperties(false)) {
      if (attribute instanceof AttributeType attributeType
          && typeToName.containsKey(attributeType.getValueClass())) {
        featureTypeBuilder.addAttribute(attributeType.getValueClass()).setName(attribute.getName());
      }
    }
    return featureTypeBuilder.build();
  }

  @Override
  public Resource add(Resource resource) throws DataStoreException {
    if (resource instanceof FeatureSet featureSet) {

      try (var connection = dataSource.getConnection()) {
        var featureType = createFeatureType(featureSet.getType());

        // Drop the table if it exists
        var dropQuery = dropTable(featureType);
        try (var dropStatement = connection.prepareStatement(dropQuery)) {
          dropStatement.execute();
        }

        // Create the table
        var createQuery = createTable(featureType);
        try (var createStatement = connection.prepareStatement(createQuery)) {
          createStatement.execute();
        }

        // Populate the table with a copy query
        PGConnection pgConnection = connection.unwrap(PGConnection.class);
        var copyQuery = copyTable(featureType);
        try (var writer = new CopyWriter(new PGCopyOutputStream(pgConnection, copyQuery))) {
          writer.writeHeader();
          var featureIterator = featureSet.features(false).iterator();
          while (featureIterator.hasNext()) {
            var feature = featureIterator.next();
            var attributes = getAttributes(featureType);
            writer.startRow(attributes.size());
            for (var attribute : attributes) {
              var name = attribute.getName().toString();
              var value = feature.getPropertyValue(name);
              if (value == null) {
                writer.writeNull();
              } else {
                writer.write(typeToHandler.get(value.getClass()), value);
              }
            }
          }
        }

        return null;
      } catch (Exception e) {
        throw new DataStoreException(e);
      }
    } else {
      throw new DataStoreException("Unsupported resource type");
    }
  }

  private List<AttributeType> getAttributes(FeatureType featureType) {
    return featureType.getProperties(false).stream().filter(this::isAttribute)
        .map(this::asAttribute).filter(this::isSupported).collect(Collectors.toList());
  }

  private boolean isAttribute(PropertyType propertyType) {
    return propertyType instanceof AttributeType;
  }

  private AttributeType asAttribute(PropertyType propertyType) {
    return (AttributeType) propertyType;
  }

  private boolean isSupported(AttributeType attributeType) {
    return typeToName.containsKey(attributeType.getValueClass());
  }

  private String createTable(FeatureType featureType) {
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE ");
    builder.append(featureType.getName());
    builder.append(" (");
    builder.append(featureType.getProperties(false).stream().filter(AttributeType.class::isInstance)
        .map(AttributeType.class::cast).map(attributeType -> attributeType.getName().toString()
            + " " + typeToName.get(attributeType.getValueClass()))
        .collect(Collectors.joining(", ")));
    builder.append(")");
    return builder.toString();
  }

  private String copyTable(FeatureType featureType) {
    StringBuilder builder = new StringBuilder();
    builder.append("COPY ");
    builder.append(featureType.getName());
    builder.append(" (");
    builder.append(featureType.getProperties(false).stream().filter(AttributeType.class::isInstance)
        .map(AttributeType.class::cast).map(attributeType -> attributeType.getName().toString())
        .collect(Collectors.joining(", ")));
    builder.append(") FROM STDIN BINARY");
    return builder.toString();
  }

  @Override
  public void remove(Resource resource) throws DataStoreException {
    if (resource instanceof FeatureSet featureSet) {
      var type = featureSet.getType();
      try (var connection = dataSource.getConnection();
          var statement = connection.createStatement()) {
        statement.executeQuery(String.format("DROP TABLE IF EXISTS %s", type.getName()));
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new DataStoreException("Unsupported resource type");
    }
  }

  private String dropTable(FeatureType type) {
    return String.format("DROP TABLE IF EXISTS %s", type.getName());
  }

}
