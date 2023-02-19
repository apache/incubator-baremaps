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
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.baremaps.database.copy.CopyWriter;
import org.apache.baremaps.database.copy.PostgisGeometryValueHandler;
import org.apache.baremaps.feature.*;
import org.locationtech.jts.geom.*;
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

  private FeatureType createFeatureType(FeatureType featureType) {
    var name = featureType.getName().replaceAll("[^a-zA-Z0-9]", "_");
    var properties = featureType.getProperties().values().stream()
        .filter(type -> typeToName.containsKey(type.getType()))
        .collect(Collectors.toMap(k -> k.getName(), v -> v));
    return new FeatureType(name, properties);
  }

  @Override
  public void write(Resource resource) throws IOException {
    if (resource instanceof ReadableFeatureSet featureSetReader) {
      try (var connection = dataSource.getConnection()) {
        var featureType = createFeatureType(featureSetReader.getType());

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
          var featureIterator = featureSetReader.read().iterator();
          while (featureIterator.hasNext()) {
            var feature = featureIterator.next();
            var attributes = getAttributes(featureType);
            writer.startRow(attributes.size());
            for (var attribute : attributes) {
              var name = attribute.getName().toString();
              var value = feature.getProperty(name);
              if (value == null) {
                writer.writeNull();
              } else {
                writer.write(typeToHandler.get(value.getClass()), value);
              }
            }
          }
        }
      } catch (SQLException e) {
        throw new IOException(e);
      }
    }
  }

  private List<PropertyType> getAttributes(FeatureType featureType) {
    return featureType.getProperties().values().stream()
        .filter(this::isSupported).collect(Collectors.toList());
  }

  private boolean isSupported(PropertyType propertyType) {
    return typeToName.containsKey(propertyType.getType());
  }

  private String createTable(FeatureType featureType) {
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE ");
    builder.append(featureType.getName());
    builder.append(" (");
    builder.append(featureType.getProperties().values().stream()
        .map(attributeType -> attributeType.getName()
            + " " + typeToName.get(attributeType.getType()))
        .collect(Collectors.joining(", ")));
    builder.append(")");
    return builder.toString();
  }

  private String copyTable(FeatureType featureType) {
    StringBuilder builder = new StringBuilder();
    builder.append("COPY ");
    builder.append(featureType.getName());
    builder.append(" (");
    builder.append(featureType.getProperties().values().stream()
        .map(propertyType -> propertyType.getName())
        .collect(Collectors.joining(", ")));
    builder.append(") FROM STDIN BINARY");
    return builder.toString();
  }

  @Override
  public void remove(Resource resource) throws IOException {
    if (resource instanceof FeatureSet featureSet) {
      var type = featureSet.getType();
      try (var connection = dataSource.getConnection();
          var statement = connection.createStatement()) {
        statement.executeQuery(String.format("DROP TABLE IF EXISTS %s", type.getName()));
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private String dropTable(FeatureType type) {
    return String.format("DROP TABLE IF EXISTS %s", type.getName());
  }

}
