package com.baremaps.tile.postgres;

import com.baremaps.config.tileset.Layer;
import com.baremaps.config.tileset.Query;
import com.baremaps.config.tileset.Tileset;
import com.baremaps.postgres.jdbc.PostgresUtils;
import java.sql.SQLException;
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

public class PostgresTilesetFactory {

  private static final String CATALOG = null;
  private static final String SCHEMA_PATTERN = "public";
  private static final String TABLE_NAME_PATTERN = "osm_%";
  private static final String COLUMN_NAME_PATTERN = null;
  private static final String[] TYPES = {"TABLE"};

  private final Jdbi jdbi;

  public PostgresTilesetFactory(DataSource dataSource) {
    this.jdbi = Jdbi.create(dataSource).installPlugin(new JpaPlugin());
  }

  public static void main(String[] args) throws SQLException {
    String url = "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps";
    DataSource dataSource = PostgresUtils.datasource(url);
    PostgresTilesetFactory postgresTilesetFactory = new PostgresTilesetFactory(dataSource);
    Tileset tileset = postgresTilesetFactory.getTileset();
    System.out.println(tileset);
  }

  public Tileset getTileset() {
    return new Tileset("postgres", listTables().stream()
        .filter(table -> table.getPrimaryKeyColumns().size() == 1)
        .filter(table -> table.getGeometryColumns().size() == 1)
        .map(this::getLayer)
        .collect(Collectors.toList())
        .toArray(new Layer[0]));
  }

  private Layer getLayer(Table table) {
    String tableName = table.getDescription().getTableName();
    String idColumn = table.getPrimaryKeyColumns().get(0).getColumnName();
    String geometryColumn = table.getGeometryColumns().get(0).getColumnName();
    String tagsColumns = table.getColumns().stream()
        .filter(column -> !idColumn.equals(column.getColumnName()))
        .filter(column -> !geometryColumn.equals(column.getColumnName()))
        .map(column -> String.format("'%1$s', %1$s::text", column.getColumnName()))
        .collect(Collectors.joining(", ", "hstore(array[", "])"));
    String sql = String.format("SELECT %s, %s, %s FROM %s", idColumn, tagsColumns, geometryColumn, tableName);
    return new Layer(tableName, new Query(0, 20, sql));
  }

  private List<Table> listTables() {
    Map<String, TableDescription> descriptions = listTableDescriptions().stream()
        .collect(Collectors.toMap(TableDescription::getTableName, Function.identity()));
    Map<String, List<TableColumn>> columns = listTableColumns().stream()
        .collect(Collectors.groupingBy(TableColumn::getTableName));
    Map<String, List<TablePrimaryKeyColumn>> primaryKeys = listTablePrimaryKeyColumns().stream()
        .collect(Collectors.groupingBy(TablePrimaryKeyColumn::getTableName));
    return descriptions.entrySet().stream()
        .map(entry -> new Table(entry.getValue(), primaryKeys.get(entry.getKey()), columns.get(entry.getKey())))
        .collect(Collectors.toList());
  }

  private List<TableDescription> listTableDescriptions() {
    return jdbi.withHandle(handle -> {
      ResultBearing resultBearing = handle
          .queryMetadata(f -> f.getTables(CATALOG, SCHEMA_PATTERN, TABLE_NAME_PATTERN, TYPES));
      return resultBearing.mapTo(TableDescription.class).list();
    });
  }

  private List<TableColumn> listTableColumns() {
    return jdbi.withHandle(handle -> {
      ResultBearing resultBearing = handle
          .queryMetadata(f -> f.getColumns(CATALOG, SCHEMA_PATTERN, TABLE_NAME_PATTERN, COLUMN_NAME_PATTERN));
      return resultBearing.mapTo(TableColumn.class).list();
    });
  }

  private List<TablePrimaryKeyColumn> listTablePrimaryKeyColumns() {
    return jdbi.withHandle(handle -> {
      ResultBearing resultBearing = handle
          .queryMetadata(f -> f.getPrimaryKeys(CATALOG, SCHEMA_PATTERN, null));
      return resultBearing.mapTo(TablePrimaryKeyColumn.class).list();
    });
  }

}

class Table {

  private TableDescription description;

  private List<TablePrimaryKeyColumn> primaryKeyColumns;

  private List<TableColumn> columns;

  Table(TableDescription description, List<TablePrimaryKeyColumn> primaryKeyColumns, List<TableColumn> columns) {
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
  private short key_seq;

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

  public short getKey_seq() {
    return key_seq;
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
        .add("key_seq=" + key_seq)
        .add("pkName='" + pkName + "'")
        .toString();
  }

}
