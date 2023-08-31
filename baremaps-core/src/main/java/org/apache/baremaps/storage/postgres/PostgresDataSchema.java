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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.baremaps.database.schema.*;
import org.apache.baremaps.database.schema.DataColumn.Type;
import org.apache.baremaps.postgres.copy.CopyWriter;
import org.apache.baremaps.postgres.copy.GeometryValueHandler;
import org.apache.baremaps.postgres.metadata.DatabaseMetadata;
import org.apache.baremaps.postgres.metadata.TableMetadata;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A schema that stores tables in a Postgres database.
 */
public class PostgresDataSchema implements DataSchema {

  private static final Logger logger = LoggerFactory.getLogger(PostgresDataSchema.class);

  private static final String[] TYPES = new String[] {"TABLE", "VIEW"};

  private final DataSource dataSource;

  /**
   * Creates a postgres schema.
   *
   * @param dataSource the data source
   */
  public PostgresDataSchema(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<String> list() throws DataTableException {
    DatabaseMetadata metadata = new DatabaseMetadata(dataSource);
    return metadata.getTableMetaData(null, "public", null, TYPES).stream()
        .map(table -> table.table().tableName())
        .collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataTable get(String name) throws DataTableException {
    var databaseMetadata = new DatabaseMetadata(dataSource);
    var tableMetadata = databaseMetadata.getTableMetaData(null, null, name, TYPES)
        .stream().findFirst();
    if (tableMetadata.isEmpty()) {
      throw new DataTableException("Table " + name + " does not exist.");
    }
    var rowType = createRowType(tableMetadata.get());
    return new PostgresDataTable(dataSource, rowType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(DataTable table) {
    try (var connection = dataSource.getConnection()) {
      var regex = "[^a-zA-Z0-9]";
      var name = table.rowType().name().replaceAll(regex, "_").toLowerCase();
      var mapping = new HashMap<String, String>();
      var properties = new ArrayList<DataColumn>();
      for (DataColumn column : table.rowType().columns()) {
        if (PostgresTypeConversion.typeToName.containsKey(column.type())) {
          var columnName = column.name().replaceAll(regex, "_").toLowerCase();
          mapping.put(columnName, column.name());
          properties.add(new DataColumnImpl(columnName, column.type()));
        }
      }

      var rowType = new DataRowTypeImpl(name, properties);

      // Drop the table if it exists
      var dropQuery = dropTable(rowType);
      logger.debug(dropQuery);
      try (var dropStatement = connection.prepareStatement(dropQuery)) {
        dropStatement.execute();
      }

      // Create the table
      var createQuery = createTable(rowType);
      logger.debug(createQuery);
      try (var createStatement = connection.prepareStatement(createQuery)) {
        createStatement.execute();
      }

      // Copy the data
      var pgConnection = connection.unwrap(PGConnection.class);
      var copyQuery = copy(rowType);
      logger.debug(copyQuery);
      try (var writer = new CopyWriter(new PGCopyOutputStream(pgConnection, copyQuery))) {
        writer.writeHeader();
        var columns = getColumns(rowType);
        var handlers = getHandlers(rowType);
        for (DataRow row : table) {
          writer.startRow(columns.size());
          for (int i = 0; i < columns.size(); i++) {
            var targetColumn = columns.get(i).name();
            var sourceColumn = mapping.get(targetColumn);
            var value = row.get(sourceColumn);
            if (value == null) {
              writer.writeNull();
            } else {
              var handler = handlers.get(i);
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
    var rowType = get(name).rowType();
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(dropTable(rowType))) {
      statement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a row type from the metadata of a postgres table.
   *
   * @param tableMetadata the table metadata
   * @return the rowType
   */
  protected static DataRowType createRowType(TableMetadata tableMetadata) {
    var name = tableMetadata.table().tableName();
    var columns = tableMetadata.columns().stream()
        .map(column -> new DataColumnImpl(column.columnName(),
            PostgresTypeConversion.nameToType.get(column.typeName())))
        .map(DataColumn.class::cast)
        .toList();
    return new DataRowTypeImpl(name, columns);
  }

  /**
   * Generate a drop table query.
   *
   * @param rowType the table name
   * @return the query
   */
  protected String dropTable(DataRowType rowType) {
    return String.format("DROP TABLE IF EXISTS \"%s\" CASCADE", rowType.name());
  }

  /**
   * Generate a create table query.
   *
   * @param rowType the row type
   * @return the query
   */
  protected String createTable(DataRowType rowType) {
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE \"");
    builder.append(rowType.name());
    builder.append("\" (");
    builder.append(rowType.columns().stream()
        .map(column -> "\"" + column.name()
            + "\" " + PostgresTypeConversion.typeToName.get(column.type()))
        .collect(Collectors.joining(", ")));
    builder.append(")");
    return builder.toString();
  }

  /**
   * Generate a copy query.
   *
   * @param rowType the row type
   * @return the query
   */
  protected String copy(DataRowType rowType) {
    var builder = new StringBuilder();
    builder.append("COPY \"");
    builder.append(rowType.name());
    builder.append("\" (");
    builder.append(rowType.columns().stream()
        .map(column -> "\"" + column.name() + "\"")
        .collect(Collectors.joining(", ")));
    builder.append(") FROM STDIN BINARY");
    return builder.toString();
  }

  /**
   * Get the columns of the row type.
   *
   * @param rowType the row type
   * @return the columns
   */
  protected List<DataColumn> getColumns(DataRowType rowType) {
    return rowType.columns().stream()
        .filter(this::isSupported)
        .collect(Collectors.toList());
  }

  /**
   * Get the handlers for the columns of the row type.
   *
   * @param rowType the row type
   * @return the handlers
   */
  protected List<BaseValueHandler> getHandlers(DataRowType rowType) {
    return getColumns(rowType).stream()
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
  protected BaseValueHandler getHandler(Type type) {
    return switch (type) {
      case STRING -> new StringValueHandler();
      case SHORT -> new ShortValueHandler<Short>();
      case INTEGER -> new IntegerValueHandler<Integer>();
      case LONG -> new LongValueHandler<Long>();
      case FLOAT -> new FloatValueHandler<Float>();
      case DOUBLE -> new DoubleValueHandler<Double>();
      case GEOMETRY -> new GeometryValueHandler();
      case POINT -> new GeometryValueHandler();
      case MULTIPOINT -> new GeometryValueHandler();
      case LINESTRING -> new GeometryValueHandler();
      case MULTILINESTRING -> new GeometryValueHandler();
      case POLYGON -> new GeometryValueHandler();
      case MULTIPOLYGON -> new GeometryValueHandler();
      case GEOMETRYCOLLECTION -> new GeometryValueHandler();
      case INET4_ADDRESS -> new Inet4AddressValueHandler();
      case INET6_ADDRESS -> new Inet6AddressValueHandler();
      case LOCAL_DATE -> new LocalDateValueHandler();
      case LOCAL_TIME -> new LocalTimeValueHandler();
      case LOCAL_DATE_TIME -> new LocalDateTimeValueHandler();
      default -> throw new IllegalArgumentException("Unsupported type: " + type);
    };
  }

  /**
   * Check if the column type is supported by postgres.
   *
   * @param column the column
   * @return true if the column type is supported
   */
  protected boolean isSupported(DataColumn column) {
    return PostgresTypeConversion.typeToName.containsKey(column.type());
  }
}
