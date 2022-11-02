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

package org.apache.baremaps.database.tile;



import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.baremaps.database.metadata.DatabaseMetadata;
import org.apache.baremaps.database.metadata.TableMetaData;

/**
 * A generator that uses PostgreSQL metadata to generate input queries for a {@code
 * PostgresTileStore}. It can be used to accelerate the creation of a tile set.
 *
 * <p>
 * As in
 * <a href="https://docs.oracle.com/javase/7/docs/api/java/sql/DatabaseMetaData.html">JDBC</a>, some
 * methods take arguments that are String patterns. These arguments all have names such as *
 * fooPattern. Within a pattern String, "%" means match any substring of 0 or more characters, and
 * "_" means match any * one character. Only metadata entries matching the search pattern are
 * returned. If a search pattern argument is set * to null, that argument's criterion will be
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
   * @param dataSource the data source
   * @param catalog the catalog
   * @param schemaPattern the schema pattern
   * @param typeNamePattern the type name pattern
   * @param columnNamePattern the column name pattern
   * @param types the types
   */
  public PostgresQueryGenerator(DataSource dataSource, String catalog, String schemaPattern,
      String typeNamePattern, String columnNamePattern, String... types) {
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
    return new DatabaseMetadata(dataSource)
        .getTableMetaData(catalog, schemaPattern, tableNamePattern, types).stream()
        .filter(table -> table.primaryKeys().size() == 1)
        .filter(table -> table.getGeometryColumns().size() == 1).map(this::getLayer).toList();
  }

  private PostgresQuery getLayer(TableMetaData table) {
    String tableSchema = table.table().tableSchem();
    String tableName = table.table().tableName();
    String layer = String.format("%s.%s", tableSchema, tableName);
    String idColumn = table.primaryKeys().get(0).columnName();
    String geometryColumn = table.getGeometryColumns().get(0).columnName();
    String tagsColumns =
        table.columns().stream().filter(column -> !idColumn.equals(column.columnName()))
            .filter(column -> !geometryColumn.equals(column.columnName()))
            .map(column -> String.format("'%1$s', %1$s::text", column.columnName()))
            .collect(Collectors.joining(", ", "hstore(array[", "])"));
    String sql = String.format("SELECT %s, %s, %s FROM %s", idColumn, tagsColumns, geometryColumn,
        tableName);
    return new PostgresQuery(layer, 0, 20, sql);
  }
}
