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

package org.apache.baremaps.database.metadata;



import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.sql.DataSource;

public class DatabaseMetadata {

  public static final String TABLE_CAT = "TABLE_CAT";
  public static final String TABLE_SCHEM = "TABLE_SCHEM";
  public static final String TABLE_NAME = "TABLE_NAME";
  public static final String TABLE_TYPE = "TABLE_TYPE";
  public static final String REMARKS = "REMARKS";
  public static final String TYPE_CAT = "TYPE_CAT";
  public static final String TYPE_SCHEM = "TYPE_SCHEM";
  public static final String TYPE_NAME = "TYPE_NAME";
  public static final String SELF_REFERENCING_COL_NAME = "SELF_REFERENCING_COL_NAME";
  public static final String REF_GENERATION = "REF_GENERATION";
  public static final String COLUMN_NAME = "COLUMN_NAME";
  public static final String DATA_TYPE = "DATA_TYPE";
  public static final String COLUMN_SIZE = "COLUMN_SIZE";
  public static final String DECIMAL_DIGITS = "DECIMAL_DIGITS";
  public static final String NUM_PREC_RADIX = "NUM_PREC_RADIX";
  public static final String NULLABLE = "NULLABLE";
  public static final String COLUMN_DEF = "COLUMN_DEF";
  public static final String KEY_SEQ = "KEY_SEQ";
  public static final String PK_NAME = "PK_NAME";
  public static final String SQL_DATA_TYPE = "SQL_DATA_TYPE";
  public static final String SQL_DATETIME_SUB = "SQL_DATETIME_SUB";
  public static final String CHAR_OCTET_LENGTH = "CHAR_OCTET_LENGTH";
  public static final String ORDINAL_POSITION = "ORDINAL_POSITION";
  public static final String IS_NULLABLE = "IS_NULLABLE";
  public static final String SCOPE_CATALOG = "SCOPE_CATALOG";
  public static final String SCOPE_SCHEMA = "SCOPE_SCHEMA";
  public static final String SCOPE_TABLE = "SCOPE_TABLE";
  public static final String SOURCE_DATA_TYPE = "SOURCE_DATA_TYPE";
  public static final String IS_AUTOINCREMENT = "IS_AUTOINCREMENT";
  public static final String IS_GENERATEDCOLUMN = "IS_GENERATEDCOLUMN";
  private final DataSource dataSource;

  public DatabaseMetadata(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public List<TableMetadata> getTableMetaData() {
    return getTableMetaData(null, null, null, null);
  }

  public List<TableMetadata> getTableMetaData(String catalog, String schema,
      String tableNamePattern, String[] types) {
    Map<String, TableResult> descriptions = getTables(catalog, schema, tableNamePattern, types)
        .stream().collect(Collectors.toMap(TableResult::tableName, Function.identity()));
    Map<String, List<ColumnResult>> columns = getColumns(catalog, schema, tableNamePattern, null)
        .stream().collect(Collectors.groupingBy(ColumnResult::tableName));
    Map<String, List<PrimaryKeyResult>> primaryKeys =
        getPrimaryKeys(catalog, schema, tableNamePattern).stream()
            .collect(Collectors.groupingBy(PrimaryKeyResult::tableName));
    return descriptions.entrySet().stream()
        .map(entry -> new TableMetadata(entry.getValue(),
            primaryKeys.getOrDefault(entry.getKey(), List.of()),
            columns.getOrDefault(entry.getKey(), List.of())))
        .toList();
  }

  private List<TableResult> getTables(String catalog, String schemaPattern, String tableNamePattern,
      String[] types) {
    var tableDescriptions = new ArrayList<TableResult>();
    try (var connection = dataSource.getConnection();
        var resultSet =
            connection.getMetaData().getTables(catalog, schemaPattern, tableNamePattern, types)) {
      while (resultSet.next()) {
        tableDescriptions.add(new TableResult(
            resultSet.getString(TABLE_CAT),
            resultSet.getString(TABLE_SCHEM),
            resultSet.getString(TABLE_NAME),
            resultSet.getString(TABLE_TYPE),
            resultSet.getString(REMARKS),
            resultSet.getString(TYPE_CAT),
            resultSet.getString(TYPE_SCHEM),
            resultSet.getString(TYPE_NAME),
            resultSet.getString(SELF_REFERENCING_COL_NAME),
            resultSet.getString(REF_GENERATION)));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return tableDescriptions;
  }

  private List<ColumnResult> getColumns(String catalog, String schemaPattern,
      String tableNamePattern, String columnNamePattern) {
    var tableColumns = new ArrayList<ColumnResult>();
    try (var connection = dataSource.getConnection();
        var resultSet = connection.getMetaData().getColumns(catalog, schemaPattern,
            tableNamePattern, columnNamePattern)) {
      while (resultSet.next()) {
        tableColumns.add(new ColumnResult(
            resultSet.getString(TABLE_CAT),
            resultSet.getString(TABLE_SCHEM),
            resultSet.getString(TABLE_NAME),
            resultSet.getString(COLUMN_NAME),
            resultSet.getInt(DATA_TYPE),
            resultSet.getString(TYPE_NAME),
            resultSet.getInt(COLUMN_SIZE),
            resultSet.getInt(DECIMAL_DIGITS),
            resultSet.getInt(NUM_PREC_RADIX),
            resultSet.getInt(NULLABLE),
            resultSet.getString(REMARKS),
            resultSet.getString(COLUMN_DEF),
            resultSet.getInt(SQL_DATA_TYPE),
            resultSet.getInt(SQL_DATETIME_SUB),
            resultSet.getInt(CHAR_OCTET_LENGTH),
            resultSet.getInt(ORDINAL_POSITION),
            resultSet.getString(IS_NULLABLE),
            resultSet.getString(SCOPE_CATALOG),
            resultSet.getString(SCOPE_SCHEMA),
            resultSet.getString(SCOPE_TABLE),
            resultSet.getShort(SOURCE_DATA_TYPE),
            resultSet.getString(IS_AUTOINCREMENT),
            resultSet.getString(IS_GENERATEDCOLUMN)));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return tableColumns;
  }

  private List<PrimaryKeyResult> getPrimaryKeys(String catalog, String schemaPattern,
      String table) {
    var tablePrimaryKeyColumns = new ArrayList<PrimaryKeyResult>();
    try (var connection = dataSource.getConnection();
        var resultSet = connection.getMetaData().getPrimaryKeys(catalog, schemaPattern, table)) {
      while (resultSet.next()) {
        tablePrimaryKeyColumns.add(new PrimaryKeyResult(
            resultSet.getString(TABLE_CAT),
            resultSet.getString(TABLE_SCHEM),
            resultSet.getString(TABLE_NAME),
            resultSet.getString(COLUMN_NAME),
            resultSet.getShort(KEY_SEQ),
            resultSet.getString(PK_NAME)));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return tablePrimaryKeyColumns;
  }
}
