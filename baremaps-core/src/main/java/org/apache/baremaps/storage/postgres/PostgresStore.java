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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.baremaps.postgres.copy.CopyWriter;
import org.apache.baremaps.postgres.copy.GeometryValueHandler;
import org.apache.baremaps.postgres.metadata.DatabaseMetadata;
import org.apache.baremaps.postgres.metadata.TableMetadata;
import org.apache.baremaps.storage.*;
import org.locationtech.jts.geom.*;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A store that stores tables in a Postgres database.
 */
public class PostgresStore implements Store {

  private static final Logger logger = LoggerFactory.getLogger(PostgresStore.class);

  private static final String[] TYPES = new String[] {"TABLE", "VIEW"};

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
      Map.entry(Inet4Address.class, "inet"),
      Map.entry(Inet6Address.class, "inet"),
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
      Map.entry("inet", InetAddress.class),
      Map.entry("date", LocalDate.class),
      Map.entry("time", LocalTime.class),
      Map.entry("timestamp", LocalDateTime.class));

  private final DataSource dataSource;

  /**
   * Creates a postgres store.
   *
   * @param dataSource the data source
   */
  public PostgresStore(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<String> list() throws TableException {
    DatabaseMetadata metadata = new DatabaseMetadata(dataSource);
    return metadata.getTableMetaData(null, "public", null, TYPES).stream()
        .map(table -> table.table().tableName())
        .collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Table get(String name) throws TableException {
    var databaseMetadata = new DatabaseMetadata(dataSource);
    var tableMetadata = databaseMetadata.getTableMetaData(null, null, name, TYPES)
        .stream().findFirst();
    if (tableMetadata.isEmpty()) {
      throw new TableException("Table " + name + " does not exist.");
    }
    var schema = createSchema(tableMetadata.get());
    return new PostgresTable(dataSource, schema);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(Table table) {
    try (var connection = dataSource.getConnection()) {
      var schema = adaptDataType(table.schema());

      // Drop the table if it exists
      var dropQuery = dropTable(schema.name());
      logger.debug(dropQuery);
      try (var dropStatement = connection.prepareStatement(dropQuery)) {
        dropStatement.execute();
      }

      // Create the table
      var createQuery = createTable(schema);
      logger.debug(createQuery);
      try (var createStatement = connection.prepareStatement(createQuery)) {
        createStatement.execute();
      }

      // Copy the data
      var pgConnection = connection.unwrap(PGConnection.class);
      var copyQuery = copy(schema);
      logger.debug(copyQuery);
      try (var writer = new CopyWriter(new PGCopyOutputStream(pgConnection, copyQuery))) {
        writer.writeHeader();
        var columns = getColumns(schema);
        var handlers = getHandlers(schema);
        for (Row row : table) {
          writer.startRow(columns.size());
          for (int i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            var handler = handlers.get(i);
            var value = row.get(column.name());
            if (value == null) {
              writer.writeNull();
            } else {
              writer.write(handler, value);
            }
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(String name) {
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(dropTable(name))) {
      statement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a schema from the metadata of a postgres table.
   *
   * @param tableMetadata the table metadata
   * @return the schema
   */
  protected static Schema createSchema(TableMetadata tableMetadata) {
    var name = tableMetadata.table().tableName();
    var columns = tableMetadata.columns().stream()
        .map(column -> new ColumnImpl(column.columnName(), nameToType.get(column.typeName())))
        .map(Column.class::cast)
        .toList();
    return new SchemaImpl(name, columns);
  }

  /**
   * Adapt the data type to postgres (e.g. use compatible names).
   *
   * @param schema the schema to adapt
   * @return the adapted schema
   */
  protected Schema adaptDataType(Schema schema) {
    var name = schema.name().replaceAll("[^a-zA-Z0-9]", "_");
    var properties = schema.columns().stream()
        .filter(column -> typeToName.containsKey(column.type()))
        .map(column -> (Column) new ColumnImpl(column.name(), column.type()))
        .toList();
    return new SchemaImpl(name, properties);
  }

  /**
   * Generate a drop table query.
   *
   * @param name the table name
   * @return the query
   */
  protected String dropTable(String name) {
    return String.format("DROP TABLE IF EXISTS \"%s\" CASCADE", name);
  }

  /**
   * Generate a create table query.
   *
   * @param schema the schema
   * @return the query
   */
  protected String createTable(Schema schema) {
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

  /**
   * Generate a copy query.
   *
   * @param schema the schema
   * @return the query
   */
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

  /**
   * Get the columns of the schema.
   *
   * @param schema the schema
   * @return the columns
   */
  protected List<Column> getColumns(Schema schema) {
    return schema.columns().stream()
        .filter(this::isSupported)
        .collect(Collectors.toList());
  }

  /**
   * Get the handlers for the columns of the schema.
   *
   * @param schema the schema
   * @return the handlers
   */
  protected List<BaseValueHandler> getHandlers(Schema schema) {
    return getColumns(schema).stream()
        .map(column -> getHandler(column.type()))
        .collect(Collectors.toList());
  }

  /**
   * Get the handler for a type. Handlers are used to write values to the copy stream. They are not
   * thread safe and should not be reused or shared between threads.
   *
   * @param type the type
   * @return the handler
   */
  protected BaseValueHandler getHandler(Class type) {
    if (type == String.class) {
      return new StringValueHandler();
    } else if (type == Short.class) {
      return new ShortValueHandler<Short>();
    } else if (type == Integer.class) {
      return new IntegerValueHandler<Integer>();
    } else if (type == Long.class) {
      return new LongValueHandler<Long>();
    } else if (type == Float.class) {
      return new FloatValueHandler<Float>();
    } else if (type == Double.class) {
      return new DoubleValueHandler<Double>();
    } else if (type == Geometry.class) {
      return new GeometryValueHandler();
    } else if (type == MultiPoint.class) {
      return new GeometryValueHandler();
    } else if (type == Point.class) {
      return new GeometryValueHandler();
    } else if (type == LineString.class) {
      return new GeometryValueHandler();
    } else if (type == MultiLineString.class) {
      return new GeometryValueHandler();
    } else if (type == Polygon.class) {
      return new GeometryValueHandler();
    } else if (type == MultiPolygon.class) {
      return new GeometryValueHandler();
    } else if (type == LinearRing.class) {
      return new GeometryValueHandler();
    } else if (type == GeometryCollection.class) {
      return new GeometryValueHandler();
    } else if (type == Inet4Address.class) {
      return new Inet4AddressValueHandler();
    } else if (type == Inet6Address.class) {
      return new Inet6AddressValueHandler();
    } else if (type == LocalDate.class) {
      return new LocalDateValueHandler();
    } else if (type == LocalTime.class) {
      return new LocalTimeValueHandler();
    } else if (type == LocalDateTime.class) {
      return new LocalDateTimeValueHandler();
    } else {
      throw new IllegalArgumentException("Unsupported type " + type);
    }
  }

  /**
   * Check if the column type is supported by postgres.
   *
   * @param column the column
   * @return true if the column type is supported
   */
  protected boolean isSupported(Column column) {
    return typeToName.containsKey(column.type());
  }
}
