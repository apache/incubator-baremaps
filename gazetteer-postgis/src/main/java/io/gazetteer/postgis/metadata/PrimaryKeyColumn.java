package io.gazetteer.postgis.metadata;

public class PrimaryKeyColumn {

  private final String tableCatalog;
  private final String tableSchema;
  private final String tableName;
  private final String columnName;
  private final Short keySeq;
  private final String pkName;

  public PrimaryKeyColumn(
      String tableCatalog,
      String tableSchema,
      String tableName,
      String columnName,
      Short keySeq,
      String pkName) {
    this.tableCatalog = tableCatalog;
    this.tableSchema = tableSchema;
    this.tableName = tableName;
    this.columnName = columnName;
    this.keySeq = keySeq;
    this.pkName = pkName;
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

  public String getColumnName() {
    return columnName;
  }

  public Short getKeySeq() {
    return keySeq;
  }

  public String getPkName() {
    return pkName;
  }
}
