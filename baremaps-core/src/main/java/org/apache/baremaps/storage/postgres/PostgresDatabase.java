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
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.baremaps.database.copy.CopyWriter;
import org.apache.baremaps.database.copy.PostgisGeometryValueHandler;
import org.apache.baremaps.dataframe.*;
import org.locationtech.jts.geom.*;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class PostgresDatabase implements DataStore {

  // TODO: Instantiate with Map.of()
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

  // TODO: Instantiate with Map.of()
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

  private DataType adaptDataType(DataType datatype) {
    var name = datatype.name().replaceAll("[^a-zA-Z0-9]", "_");
    var properties = datatype.columns().stream()
        .filter(columnType -> typeToName.containsKey(columnType.type()))
        .toList();
    return new DataTypeImpl(name, properties);
  }

  @Override
  public Collection<DataFrame> list() throws DataFrameException {
    throw new UnsupportedOperationException();
  }

  @Override
  public DataFrame get(String name) throws DataFrameException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(DataFrame dataFrame) {
    try (var connection = dataSource.getConnection()) {
      var dataType = adaptDataType(dataFrame.dataType());

      // Drop the table if it exists
      var dropQuery = dropTable(dataType);
      try (var dropStatement = connection.prepareStatement(dropQuery)) {
        dropStatement.execute();
      }

      // Create the table
      var createQuery = createTable(dataType);
      try (var createStatement = connection.prepareStatement(createQuery)) {
        createStatement.execute();
      }

      // Populate the table with a copy query
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      var copyQuery = copyTable(dataType);
      try (var writer = new CopyWriter(new PGCopyOutputStream(pgConnection, copyQuery))) {
        writer.writeHeader();
        var featureIterator = dataFrame.iterator();
        while (featureIterator.hasNext()) {
          var feature = featureIterator.next();
          var attributes = getAttributes(dataType);
          writer.startRow(attributes.size());
          for (var attribute : attributes) {
            var name = attribute.name().toString();
            var value = feature.get(name);
            if (value == null) {
              writer.writeNull();
            } else {
              writer.write(typeToHandler.get(value.getClass()), value);
            }
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<Column> getAttributes(DataType dataType) {
    return dataType.columns().stream()
        .filter(this::isSupported).collect(Collectors.toList());
  }

  private boolean isSupported(Column column) {
    return typeToName.containsKey(column.type());
  }

  private String createTable(DataType dataType) {
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE ");
    builder.append(dataType.name());
    builder.append(" (");
    builder.append(dataType.columns().stream()
        .map(column -> column.name()
            + " " + typeToName.get(column.type()))
        .collect(Collectors.joining(", ")));
    builder.append(")");
    return builder.toString();
  }

  private String copyTable(DataType dataType) {
    StringBuilder builder = new StringBuilder();
    builder.append("COPY ");
    builder.append(dataType.name());
    builder.append(" (");
    builder.append(dataType.columns().stream()
        .map(column -> column.name())
        .collect(Collectors.joining(", ")));
    builder.append(") FROM STDIN BINARY");
    return builder.toString();
  }

  @Override
  public void remove(String name) {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      statement.executeQuery(String.format("DROP TABLE IF EXISTS %s CASCADE", name));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private String dropTable(DataType type) {
    return String.format("DROP TABLE IF EXISTS %s CASCADE", type.name());
  }
}
