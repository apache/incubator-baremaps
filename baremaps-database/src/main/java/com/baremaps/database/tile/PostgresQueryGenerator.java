/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.database.tile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.sql.DataSource;

/**
 * A generator that uses PostgreSQL metadata to generate input queries for a {@code PostgresTileStore}. It can be used
 * to accelerate the creation of a tile set.
 *
 * <p>As in <a
 * href="https://docs.oracle.com/javase/7/docs/api/java/sql/DatabaseMetaData.html">JDBC</a>, some methods take arguments
 * that are String patterns. These arguments all have names such as * fooPattern. Within a pattern String, "%" means
 * match any substring of 0 or more characters, and "_" means match any * one character. Only metadata entries matching
 * the search pattern are returned. If a search pattern argument is set * to null, that argument's criterion will be
 * dropped from the search.
 */
public class PostgresQueryGenerator {

  private final String catalog;
  private final String schemaPattern;
  private final String tableNamePattern;
  private final String columnNamePattern;
  private final String[] types;

  private final DataSource dataSource;

  /**
   * Constructs a {@code PostgresQueryGenerator}.
   *
   * @param dataSource the data source
   */
  public PostgresQueryGenerator(DataSource dataSource) {
    this(dataSource, null, null, null, null, null);
  }

  /**
   * Constructs a {@code PostgresQueryGenerator}.
   *
   * @param dataSource        the data source
   * @param catalog           the catalog
   * @param schemaPattern     the schema pattern
   * @param typeNamePattern   the type name pattern
   * @param columnNamePattern the column name pattern
   * @param types             the types
   */
  public PostgresQueryGenerator(
      DataSource dataSource,
      String catalog,
      String schemaPattern,
      String typeNamePattern,
      String columnNamePattern,
      String... types) {
    this.dataSource = dataSource;
    this.catalog = catalog;
    this.schemaPattern = schemaPattern;
    this.tableNamePattern = typeNamePattern;
    this.columnNamePattern = columnNamePattern;
    this.types = types;
  }

  /**
   * Generates the queries.
   *
   * @return the queries
   */
  public List<PostgresQuery> generate() {
    return listTables().stream()
        .filter(table -> table.getPrimaryKeyColumns().size() == 1)
        .filter(table -> table.getGeometryColumns().size() == 1)
        .map(this::getLayer)
        .toList();
  }

  private PostgresQuery getLayer(Table table) {
    String tableSchema = table.getDescription().tableSchem();
    String tableName = table.getDescription().tableName();
    String layer = String.format("%s.%s", tableSchema, tableName);
    String idColumn = table.getPrimaryKeyColumns().get(0).columnName();
    String geometryColumn = table.getGeometryColumns().get(0).columnName();
    String tagsColumns =
        table.getColumns().stream()
            .filter(column -> !idColumn.equals(column.columnName()))
            .filter(column -> !geometryColumn.equals(column.columnName()))
            .map(column -> String.format("'%1$s', %1$s::text", column.columnName()))
            .collect(Collectors.joining(", ", "hstore(array[", "])"));
    String sql =
        String.format(
            "SELECT %s, %s, %s FROM %s", idColumn, tagsColumns, geometryColumn, tableName);
    return new PostgresQuery(layer, 0, 20, sql);
  }

  private List<Table> listTables() {
    Map<String, TableDescription> descriptions =
        listTableDescriptions().stream()
            .collect(Collectors.toMap(TableDescription::tableName, Function.identity()));
    Map<String, List<TableColumn>> columns =
        listTableColumns().stream().collect(Collectors.groupingBy(TableColumn::tableName));
    Map<String, List<TablePrimaryKeyColumn>> primaryKeys =
        listTablePrimaryKeyColumns().stream()
            .collect(Collectors.groupingBy(TablePrimaryKeyColumn::tableName));
    return descriptions.entrySet().stream()
        .map(
            entry ->
                new Table(
                    entry.getValue(),
                    primaryKeys.getOrDefault(entry.getKey(), List.of()),
                    columns.getOrDefault(entry.getKey(), List.of())))
        .toList();
  }

  private List<TableDescription> listTableDescriptions() {
    var tableDescriptions = new ArrayList<TableDescription>();
    try (var connection = dataSource.getConnection();
        var resultSet = connection.getMetaData().getTables(catalog, schemaPattern, tableNamePattern, types)) {
      while (resultSet.next()) {
        tableDescriptions.add(new TableDescription(
            resultSet.getString("TABLE_CAT"),
            resultSet.getString("TABLE_SCHEM"),
            resultSet.getString("TABLE_NAME"),
            resultSet.getString("TABLE_TYPE"),
            resultSet.getString("REMARKS"),
            resultSet.getString("TYPE_CAT"),
            resultSet.getString("TYPE_SCHEM"),
            resultSet.getString("TYPE_NAME"),
            resultSet.getString("SELF_REFERENCING_COL_NAME"),
            resultSet.getString("REF_GENERATION")));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return tableDescriptions;
  }



  private List<TableColumn> listTableColumns() {
    var tableColumns = new ArrayList<TableColumn>();
    try (var connection = dataSource.getConnection();
        var resultSet = connection.getMetaData().getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern)) {
      while (resultSet.next()) {
        tableColumns.add(new TableColumn(
            resultSet.getString("TABLE_CAT"),
            resultSet.getString("TABLE_SCHEM"),
            resultSet.getString("TABLE_NAME"),
            resultSet.getString("COLUMN_NAME"),
            resultSet.getInt("DATA_TYPE"),
            resultSet.getString("TYPE_NAME"),
            resultSet.getInt("SQL_DATA_TYPE"),
            resultSet.getInt("ORDINAL_POSITION")));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return tableColumns;
  }

  private List<TablePrimaryKeyColumn> listTablePrimaryKeyColumns() {
    var tablePrimaryKeyColumns = new ArrayList<TablePrimaryKeyColumn>();
    try (var connection = dataSource.getConnection();
        var resultSet = connection.getMetaData().getPrimaryKeys(catalog, schemaPattern, null)) {
      while (resultSet.next()) {
        tablePrimaryKeyColumns.add(new TablePrimaryKeyColumn(
            resultSet.getString("TABLE_CAT"),
            resultSet.getString("TABLE_SCHEM"),
            resultSet.getString("TABLE_NAME"),
            resultSet.getString("COLUMN_NAME"),
            resultSet.getShort("KEY_SEQ"),
            resultSet.getString("PK_NAME")));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return tablePrimaryKeyColumns;
  }
}

record Table (TableDescription description,List<TablePrimaryKeyColumn> primaryKeyColumns,List<TableColumn> columns) {

  public TableDescription getDescription() {
    return description;
  }

  public List<TablePrimaryKeyColumn> getPrimaryKeyColumns() {
    return primaryKeyColumns;
  }

  public List<TableColumn> getColumns() {
    return columns;
  }

  public List<TableColumn> getGeometryColumns() {
    return columns.stream().filter(column -> "geometry".equals(column.typeName())).toList();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Table.class.getSimpleName() + "[", "]")
        .add("description=" + description)
        .add("primaryKeyColumns=" + primaryKeyColumns)
        .add("columns=" + columns)
        .toString();
  }
}

record TableDescription(
    String tableCat,
    String tableSchem,
    String tableName,
    String tableType,
    String remarks,
    String typeCat,
    String typeSchem,
    String typeName,
    String selfReferencingColName,
    String refGeneration) {

}

record TableColumn(
    String tableCat,
    String tableSchem,
    String tableName,
    String columnName,
    int dataType,
    String typeName,
    int sqlDataType,
    int ordinalPosition) {
}

record TablePrimaryKeyColumn(
    String tableCat,
    String tableSchem,
    String tableName,
    String columnName,
    short keySeq,
    String pkName) {

}





