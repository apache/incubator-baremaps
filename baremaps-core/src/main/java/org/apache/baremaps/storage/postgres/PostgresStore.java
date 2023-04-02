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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.baremaps.database.copy.CopyWriter;
import org.apache.baremaps.database.copy.PostgisGeometryValueHandler;
import org.apache.baremaps.database.metadata.DatabaseMetadata;
import org.apache.baremaps.storage.*;
import org.locationtech.jts.geom.*;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresStore implements Store {

  private static final Logger logger = LoggerFactory.getLogger(PostgresStore.class);

  protected static final Map<Class, String> typeToName = Map.ofEntries(
      Map.entry(String.class, "varchar"),
      Map.entry(Short.class, "int2"),
      Map.entry(Integer.class, "int4"),
      Map.entry(Long.class, "int8"),
      Map.entry(Float.class, "float4"),
      Map.entry(Double.class, "float8"),
      Map.entry(Geometry.class, "geometry"),
      Map.entry(MultiPoint.class, "geometry"),
      Map.entry(Point.class, "geometry"),
      Map.entry(LineString.class, "geometry"),
      Map.entry(MultiLineString.class, "geometry"),
      Map.entry(Polygon.class, "geometry"),
      Map.entry(MultiPolygon.class, "geometry"),
      Map.entry(LinearRing.class, "geometry"),
      Map.entry(GeometryCollection.class, "geometry"),
      Map.entry(LocalDate.class, "date"),
      Map.entry(LocalTime.class, "time"),
      Map.entry(LocalDateTime.class, "timestamp"));

  protected static final Map<String, Class> nameToType = Map.ofEntries(
      Map.entry("varchar", String.class),
      Map.entry("int2", Short.class),
      Map.entry("int4", Integer.class),
      Map.entry("int8", Long.class),
      Map.entry("float4", Float.class),
      Map.entry("float8", Double.class),
      Map.entry("geometry", Geometry.class),
      Map.entry("date", LocalDate.class),
      Map.entry("time", LocalTime.class),
      Map.entry("timestamp", LocalDateTime.class));

  public static final Map<Class, BaseValueHandler> typeToHandler = Map.ofEntries(
      Map.entry(String.class, new StringValueHandler()),
      Map.entry(Short.class, new ShortValueHandler()),
      Map.entry(Integer.class, new IntegerValueHandler()),
      Map.entry(Long.class, new LongValueHandler()),
      Map.entry(Float.class, new FloatValueHandler()),
      Map.entry(Double.class, new DoubleValueHandler()),
      Map.entry(Geometry.class, new PostgisGeometryValueHandler()),
      Map.entry(MultiPoint.class, new PostgisGeometryValueHandler()),
      Map.entry(Point.class, new PostgisGeometryValueHandler()),
      Map.entry(LineString.class, new PostgisGeometryValueHandler()),
      Map.entry(MultiLineString.class, new PostgisGeometryValueHandler()),
      Map.entry(Polygon.class, new PostgisGeometryValueHandler()),
      Map.entry(MultiPolygon.class, new PostgisGeometryValueHandler()),
      Map.entry(LinearRing.class, new PostgisGeometryValueHandler()),
      Map.entry(GeometryCollection.class, new PostgisGeometryValueHandler()),
      Map.entry(LocalDate.class, new LocalDateValueHandler()),
      Map.entry(LocalTime.class, new LocalTimeValueHandler()),
      Map.entry(LocalDateTime.class, new LocalDateTimeValueHandler()));

  private final DataSource dataSource;

  public PostgresStore(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  private Schema adaptDataType(Schema schema) {
    var name = schema.name().replaceAll("[^a-zA-Z0-9]", "_");
    var properties = schema.columns().stream()
        .filter(columnType -> typeToName.containsKey(columnType.type()))
        .toList();
    return new SchemaImpl(name, properties);
  }

  @Override
  public Collection<String> list() throws TableException {
    DatabaseMetadata metadata = new DatabaseMetadata(dataSource);
    return metadata.getTableMetaData(null, "public", null, null).stream()
        .map(table -> table.table().tableName())
        .collect(Collectors.toList());
  }

  @Override
  public Table get(String name) throws TableException {
    var databaseMetadata = new DatabaseMetadata(dataSource);
    var tableMetadata = databaseMetadata.getTableMetaData(null, null, name, null)
        .stream().findFirst();
    if (tableMetadata.isEmpty()) {
      throw new TableException("Table " + name + " does not exist.");
    }
    return new PostgresTable(dataSource, tableMetadata.get());
  }

  @Override
  public void add(Table table) {
    try (var connection = dataSource.getConnection()) {
      var schema = adaptDataType(table.schema());

      // Drop the table if it exists
      var dropQuery = dropTable(schema.name());
      logger.info(dropQuery);
      try (var dropStatement = connection.prepareStatement(dropQuery)) {
        dropStatement.execute();
      }

      // Create the table
      var createQuery = createTable(schema);
      logger.info(createQuery);
      try (var createStatement = connection.prepareStatement(createQuery)) {
        createStatement.execute();
      }

      // Copy the data
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      var copyQuery = copy(schema);
      logger.info(copyQuery);
      try (var writer = new CopyWriter(new PGCopyOutputStream(pgConnection, copyQuery))) {
        writer.writeHeader();
        var rowIterator = table.iterator();
        while (rowIterator.hasNext()) {
          var row = rowIterator.next();
          var columns = getColumns(schema);
          writer.startRow(columns.size());
          for (Column column : columns) {
            var value = row.get(column.name());
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

  @Override
  public void remove(String name) {
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(dropTable(name))) {
      statement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private String dropTable(String name) {
    return String.format("DROP TABLE IF EXISTS \"%s\" CASCADE", name);
  }

  private String createTable(Schema schema) {
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE \"");
    builder.append(schema.name());
    builder.append("\" (");
    builder.append(schema.columns().stream()
        .map(column -> "\"" + column.name()
            + "\" " + typeToName.get(column.type()))
        .collect(Collectors.joining(", ")));
    builder.append(")");
    return builder.toString();
  }

  protected String copy(Schema schema) {
    var builder = new StringBuilder();
    builder.append("COPY \"");
    builder.append(schema.name());
    builder.append("\" (");
    builder.append(schema.columns().stream()
        .map(column -> "\"" + column.name() + "\"")
        .collect(Collectors.joining(", ")));
    builder.append(") FROM STDIN BINARY");
    return builder.toString();
  }

  protected List<Column> getColumns(Schema schema) {
    return schema.columns().stream()
        .filter(this::isSupported)
        .collect(Collectors.toList());
  }

  protected boolean isSupported(Column column) {
    return typeToName.containsKey(column.type());
  }
}
