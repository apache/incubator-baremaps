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

package com.baremaps.core.tile;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultBearing;
import org.jdbi.v3.jpa.JpaPlugin;

/**
 * A generator that uses PostgreSQL metadata to generate input queries for a {@code
 * PostgresTileStore}. It can be used to accelerate the creation of a tile set.
 *
 * <p>As in <a
 * href="https://docs.oracle.com/javase/7/docs/api/java/sql/DatabaseMetaData.html">JDBC</a>, some
 * methods take arguments that are String patterns. These arguments all have names such as *
 * fooPattern. Within a pattern String, "%" means match any substring of 0 or more characters, and
 * "_" means match any * one character. Only metadata entries matching the search pattern are
 * returned. If a search pattern argument is set * to null, that argument's criterion will be
 * dropped from the search.
 */
public class PostgresQueryGenerator {

  private final String catalog;
  private final String schemaPattern;
  private final String typeNamePattern;
  private final String columnNamePattern;
  private final String[] types;

  private final Jdbi jdbi;

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
   * @param dataSource the data source
   * @param catalog the catalog
   * @param schemaPattern the schema pattern
   * @param typeNamePattern the type name pattern
   * @param columnNamePattern the column name pattern
   * @param types the types
   */
  public PostgresQueryGenerator(
      DataSource dataSource,
      String catalog,
      String schemaPattern,
      String typeNamePattern,
      String columnNamePattern,
      String... types) {
    this.jdbi = Jdbi.create(dataSource).installPlugin(new JpaPlugin());
    this.catalog = catalog;
    this.schemaPattern = schemaPattern;
    this.typeNamePattern = typeNamePattern;
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
        .collect(Collectors.toList());
  }

  private PostgresQuery getLayer(Table table) {
    String tableSchema = table.getDescription().getTableSchem();
    String tableName = table.getDescription().getTableName();
    String layer = String.format("%s.%s", tableSchema, tableName);
    String idColumn = table.getPrimaryKeyColumns().get(0).getColumnName();
    String geometryColumn = table.getGeometryColumns().get(0).getColumnName();
    String tagsColumns =
        table.getColumns().stream()
            .filter(column -> !idColumn.equals(column.getColumnName()))
            .filter(column -> !geometryColumn.equals(column.getColumnName()))
            .map(column -> String.format("'%1$s', %1$s::text", column.getColumnName()))
            .collect(Collectors.joining(", ", "hstore(array[", "])"));
    String sql =
        String.format(
            "SELECT %s, %s, %s FROM %s", idColumn, tagsColumns, geometryColumn, tableName);
    return new PostgresQuery(layer, 0, 20, sql);
  }

  private List<Table> listTables() {
    Map<String, TableDescription> descriptions =
        listTableDescriptions().stream()
            .collect(Collectors.toMap(TableDescription::getTableName, Function.identity()));
    Map<String, List<TableColumn>> columns =
        listTableColumns().stream().collect(Collectors.groupingBy(TableColumn::getTableName));
    Map<String, List<TablePrimaryKeyColumn>> primaryKeys =
        listTablePrimaryKeyColumns().stream()
            .collect(Collectors.groupingBy(TablePrimaryKeyColumn::getTableName));
    return descriptions.entrySet().stream()
        .map(
            entry ->
                new Table(
                    entry.getValue(),
                    primaryKeys.getOrDefault(entry.getKey(), List.of()),
                    columns.getOrDefault(entry.getKey(), List.of())))
        .collect(Collectors.toList());
  }

  private List<TableDescription> listTableDescriptions() {
    return jdbi.withHandle(
        handle -> {
          ResultBearing resultBearing =
              handle.queryMetadata(
                  f -> f.getTables(catalog, schemaPattern, typeNamePattern, types));
          return resultBearing.mapTo(TableDescription.class).list();
        });
  }

  private List<TableColumn> listTableColumns() {
    return jdbi.withHandle(
        handle -> {
          ResultBearing resultBearing =
              handle.queryMetadata(
                  f -> f.getColumns(catalog, schemaPattern, typeNamePattern, columnNamePattern));
          return resultBearing.mapTo(TableColumn.class).list();
        });
  }

  private List<TablePrimaryKeyColumn> listTablePrimaryKeyColumns() {
    return jdbi.withHandle(
        handle -> {
          ResultBearing resultBearing =
              handle.queryMetadata(f -> f.getPrimaryKeys(catalog, schemaPattern, null));
          return resultBearing.mapTo(TablePrimaryKeyColumn.class).list();
        });
  }
}

class Table {

  private TableDescription description;

  private List<TablePrimaryKeyColumn> primaryKeyColumns;

  private List<TableColumn> columns;

  Table(
      TableDescription description,
      List<TablePrimaryKeyColumn> primaryKeyColumns,
      List<TableColumn> columns) {
    this.description = description;
    this.primaryKeyColumns = primaryKeyColumns;
    this.columns = columns;
  }

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
    return columns.stream()
        .filter(column -> "geometry".equals(column.getTypeName()))
        .collect(Collectors.toList());
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

@Entity
class TableDescription {

  @Column(name = "table_cat")
  private String tableCat;

  @Column(name = "table_schem")
  private String tableSchem;

  @Column(name = "table_name")
  private String tableName;

  @Column(name = "table_type")
  private String tableType;

  @Column(name = "remarks")
  private String remarks;

  @Column(name = "type_cat")
  private String typeCat;

  @Column(name = "type_schem")
  private String typeSchem;

  @Column(name = "type_name")
  private String typeName;

  @Column(name = "self_referencing_col_name")
  private String selfReferencingColName;

  @Column(name = "ref_generation")
  private String refGeneration;

  public String getTableCat() {
    return tableCat;
  }

  public String getTableSchem() {
    return tableSchem;
  }

  public String getTableName() {
    return tableName;
  }

  public String getTableType() {
    return tableType;
  }

  public String getRemarks() {
    return remarks;
  }

  public String getTypeCat() {
    return typeCat;
  }

  public String getTypeSchem() {
    return typeSchem;
  }

  public String getTypeName() {
    return typeName;
  }

  public String getSelfReferencingColName() {
    return selfReferencingColName;
  }

  public String getRefGeneration() {
    return refGeneration;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TableDescription.class.getSimpleName() + "[", "]")
        .add("TABLE_CAT='" + tableCat + "'")
        .add("TABLE_SCHEM='" + tableSchem + "'")
        .add("TABLE_NAME='" + tableName + "'")
        .add("TABLE_TYPE='" + tableType + "'")
        .add("REMARKS='" + remarks + "'")
        .add("TYPE_CAT='" + typeCat + "'")
        .add("TYPE_SCHEM='" + typeSchem + "'")
        .add("TYPE_NAME='" + typeName + "'")
        .add("SELF_REFERENCING_COL_NAME='" + selfReferencingColName + "'")
        .add("REF_GENERATION='" + refGeneration + "'")
        .toString();
  }
}

@Entity
class TableColumn {

  @Column(name = "table_cat")
  private String tableCat;

  @Column(name = "table_schem")
  private String tableSchem;

  @Column(name = "table_name")
  private String tableName;

  @Column(name = "column_name")
  private String columnName;

  @Column(name = "data_type")
  private int dataType;

  @Column(name = "type_name")
  private String typeName;

  @Column(name = "sql_data_type")
  private int sqlDataType;

  @Column(name = "ordinal_position")
  private int ordinalPosition;

  public String getTableCat() {
    return tableCat;
  }

  public String getTableSchem() {
    return tableSchem;
  }

  public String getTableName() {
    return tableName;
  }

  public String getColumnName() {
    return columnName;
  }

  public int getDataType() {
    return dataType;
  }

  public String getTypeName() {
    return typeName;
  }

  public int getSqlDataType() {
    return sqlDataType;
  }

  public int getOrdinalPosition() {
    return ordinalPosition;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TableColumn.class.getSimpleName() + "[", "]")
        .add("tableCat='" + tableCat + "'")
        .add("tableSchem='" + tableSchem + "'")
        .add("tableName='" + tableName + "'")
        .add("columnName='" + columnName + "'")
        .add("dataType=" + dataType)
        .add("typeName='" + typeName + "'")
        .add("ordinalPosition=" + ordinalPosition)
        .toString();
  }
}

@Entity
class TablePrimaryKeyColumn {

  @Column(name = "table_cat")
  private String tableCat;

  @Column(name = "table_schem")
  private String tableSchem;

  @Column(name = "table_name")
  private String tableName;

  @Column(name = "column_name")
  private String columnName;

  @Column(name = "key_seq")
  private short keySeq;

  @Column(name = "pk_name")
  private String pkName;

  public String getTableCat() {
    return tableCat;
  }

  public String getTableSchem() {
    return tableSchem;
  }

  public String getTableName() {
    return tableName;
  }

  public String getColumnName() {
    return columnName;
  }

  public short getKeySeq() {
    return keySeq;
  }

  public String getPkName() {
    return pkName;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TablePrimaryKeyColumn.class.getSimpleName() + "[", "]")
        .add("tableCat='" + tableCat + "'")
        .add("tableSchem='" + tableSchem + "'")
        .add("tableName='" + tableName + "'")
        .add("columnName='" + columnName + "'")
        .add("key_seq=" + keySeq)
        .add("pkName='" + pkName + "'")
        .toString();
  }
}
