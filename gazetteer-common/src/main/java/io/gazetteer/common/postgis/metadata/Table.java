package io.gazetteer.common.postgis.metadata;

public class Table {

  private final String tableCatalog;
  private final String tableSchema;
  private final String tableName;
  private final String tableType;
  private final String remarks;

  public Table(String tableCatalog, String tableSchema, String tableName, String tableType, String remarks) {
    this.tableCatalog = tableCatalog;
    this.tableSchema = tableSchema;
    this.tableName = tableName;
    this.tableType = tableType;
    this.remarks = remarks;
  }

  public String getTableCatalog() {
    return tableCatalog;
  }

  public String getTableSchema() {
    return tableSchema;
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
}
