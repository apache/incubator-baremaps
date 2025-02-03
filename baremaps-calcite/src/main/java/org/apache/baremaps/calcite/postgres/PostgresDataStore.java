/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.calcite.postgres;


import de.bytefish.pgbulkinsert.pgsql.handlers.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.baremaps.calcite.*;
import org.apache.baremaps.calcite.DataColumn.Type;
import org.apache.baremaps.postgres.copy.CopyWriter;
import org.apache.baremaps.postgres.copy.EnvelopeValueHandler;
import org.apache.baremaps.postgres.copy.GeometryValueHandler;
import org.apache.baremaps.postgres.metadata.DatabaseMetadata;
import org.apache.baremaps.postgres.metadata.TableMetadata;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link DataStore} that stores {@link DataTable}s in the tables of a Postgres database.
 */
public class PostgresDataStore implements DataStore {

  public static final String REGEX = "[^a-zA-Z0-9]";

  private static final Logger logger = LoggerFactory.getLogger(PostgresDataStore.class);

  private static final String[] TYPES = new String[] {"TABLE", "VIEW"};

  private final DataSource dataSource;

  /**
   * Creates a postgres data store with the given data source.
   *
   * @param dataSource the data source
   */
  public PostgresDataStore(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> list() throws DataStoreException {
    try {
      DatabaseMetadata metadata = new DatabaseMetadata(dataSource);
      return metadata.getTableMetaData(null, "public", null, TYPES).stream()
          .map(table -> table.table().tableName())
          .toList();
    } catch (SQLException e) {
      throw new DataStoreException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataTable get(String name) throws DataStoreException {
    try {
      var databaseMetadata = new DatabaseMetadata(dataSource);
      var postgresName = name.replaceAll(REGEX, "_").toLowerCase();
      var tableMetadata = databaseMetadata.getTableMetaData(null, null, postgresName, TYPES)
          .stream().findFirst();
      if (tableMetadata.isEmpty()) {
        throw new DataStoreException("Table " + name + " does not exist.");
      }
      var schema = createSchema(tableMetadata.get());
      return new PostgresDataTable(dataSource, schema);
    } catch (SQLException e) {
      throw new DataStoreException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(DataTable table) {
    var name = table.schema().name().replaceAll(REGEX, "_").toLowerCase();
    add(name, table);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(String name, DataTable table) {
    try (var connection = dataSource.getConnection()) {
      var mapping = new HashMap<String, String>();
      var properties = new ArrayList<DataColumn>();
      for (DataColumn column : table.schema().columns()) {
        if (PostgresTypeConversion.typeToName.containsKey(column.type())) {
          var columnName = column.name().replaceAll(REGEX, "_").toLowerCase();
          mapping.put(columnName, column.name());
          properties.add(new DataColumnFixed(columnName, column.cardinality(), column.type()));
        }
      }

      var schema = new DataSchema(name, properties);

      // Create the table
      var createQuery = createTable(schema);
      logger.debug(createQuery);
      try (var createStatement = connection.prepareStatement(createQuery)) {
        createStatement.execute();
      }

      // Truncate the table
      var truncateQuery = truncateTable(schema);
      logger.debug(truncateQuery);
      try (var truncateStatement = connection.prepareStatement(truncateQuery)) {
        truncateStatement.execute();
      }

      // Copy the data
      var pgConnection = connection.unwrap(PGConnection.class);
      var copyQuery = copy(schema);
      logger.debug(copyQuery);
      try (var writer = new CopyWriter(new PGCopyOutputStream(pgConnection, copyQuery))) {
        writer.writeHeader();
        var columns = getColumns(schema);
        var handlers = getHandlers(schema);
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
      throw new DataStoreException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(String name) {
    var schema = get(name).schema();
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(dropTable(schema))) {
      statement.execute();
    } catch (SQLException e) {
      throw new DataStoreException(e);
    }
  }

  /**
   * Creates a schema from the metadata of a postgres table.
   *
   * @param tableMetadata the table metadata
   * @return the schema
   */
  protected static DataSchema createSchema(TableMetadata tableMetadata) {
    var name = tableMetadata.table().tableName();
    var columns = tableMetadata.columns().stream()
        .map(column -> new DataColumnFixed(
            column.columnName(),
            column.isNullable().equals("NO") ? DataColumn.Cardinality.REQUIRED
                : DataColumn.Cardinality.OPTIONAL,
            PostgresTypeConversion.nameToType.get(column.typeName())))
        .map(DataColumn.class::cast)
        .toList();
    return new DataSchema(name, columns);
  }

  /**
   * Generate a drop table query.
   *
   * @param schema the schema
   * @return the query
   */
  protected String dropTable(DataSchema schema) {
    return String.format("DROP TABLE IF EXISTS \"%s\" CASCADE", schema.name());
  }

  /**
   * Generate a truncate table query.
   *
   * @param schema the schema
   * @return the query
   */
  protected String truncateTable(DataSchema schema) {
    return String.format("TRUNCATE TABLE \"%s\" CASCADE", schema.name());
  }

  /**
   * Generate a create table query.
   *
   * @param schema the schema
   * @return the query
   */
  protected String createTable(DataSchema schema) {
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE IF NOT EXISTS \"");
    builder.append(schema.name());
    builder.append("\" (");
    builder.append(schema.columns().stream()
        .map(PostgresDataStore::getColumnType)
        .collect(Collectors.joining(", ")));
    builder.append(")");
    return builder.toString();
  }

  private static String getColumnType(DataColumn column) {
    String columnName = column.name();
    String columnType = PostgresTypeConversion.typeToName.get(column.type());
    String columnArray = column.cardinality() == DataColumn.Cardinality.REPEATED ? "[]" : "";
    String columnNull = column.cardinality() == DataColumn.Cardinality.REQUIRED ? "NOT NULL" : "";
    return String.format("\"%s\" %s%s %s", columnName, columnType, columnArray, columnNull).strip();
  }

  /**
   * Generate a copy query.
   *
   * @param schema the schema
   * @return the query
   */
  protected String copy(DataSchema schema) {
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
  protected List<DataColumn> getColumns(DataSchema schema) {
    return schema.columns().stream()
        .filter(this::isSupported)
        .toList();
  }

  /**
   * Get the handlers for the columns of the schema.
   *
   * @param schema the schema
   * @return the handlers
   */
  protected List<BaseValueHandler> getHandlers(DataSchema schema) {
    return getColumns(schema).stream()
        .map(column -> getHandler(column.type()))
        .toList();
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
      case INET4_ADDRESS -> new Inet4AddressValueHandler();
      case INET6_ADDRESS -> new Inet6AddressValueHandler();
      case LOCAL_DATE -> new LocalDateValueHandler();
      case LOCAL_TIME -> new LocalTimeValueHandler();
      case LOCAL_DATE_TIME -> new LocalDateTimeValueHandler();
      case GEOMETRY, POINT, MULTIPOINT, LINESTRING, MULTILINESTRING, POLYGON, MULTIPOLYGON, GEOMETRYCOLLECTION -> new GeometryValueHandler();
      case ENVELOPE -> new EnvelopeValueHandler();
      case NESTED -> new JsonbValueHandler();
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
