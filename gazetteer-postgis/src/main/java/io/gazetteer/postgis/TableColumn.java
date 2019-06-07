package io.gazetteer.postgis;

public final class TableColumn {

  private final String tableCatalog;
  private final String tableSchema;
  private final String tableName;
  private final Integer dataType;
  private final String columnName;
  private final String typeName;
  private final Integer columnSize;
  private final Integer bufferLength;
  private final Integer decimalDigits;
  private final Integer numPrecRadix;
  private final Integer nullable;
  private final String remarks;
  private final String columnDef;
  private final Integer sqlDataType;
  private final Integer sqlDateTimeSub;
  private final Integer charOctetLength;
  private final Integer ordinalPosition;
  private final String isNullable;
  private final String scopeSchema;
  private final String scopeTable;
  private final Short sourceDataType;
  private final String isAutoIncrement;

  public TableColumn(
      String tableCatalog,
      String tableSchema,
      String tableName,
      String columnName,
      Integer dataType,
      String typeName,
      Integer columnSize,
      Integer bufferLength,
      Integer decimalDigits,
      Integer numPrecRadix,
      Integer nullable,
      String remarks,
      String columnDef,
      Integer sqlDataType,
      Integer sqlDateTimeSub,
      Integer charOctetLength,
      Integer ordinalPosition,
      String isNullable,
      String scopeSchema,
      String scopeTable,
      Short sourceDataType,
      String isAutoIncrement) {
    this.tableCatalog = tableCatalog;
    this.tableSchema = tableSchema;
    this.tableName = tableName;
    this.columnName = columnName;
    this.dataType = dataType;
    this.typeName = typeName;
    this.columnSize = columnSize;
    this.bufferLength = bufferLength;
    this.decimalDigits = decimalDigits;
    this.numPrecRadix = numPrecRadix;
    this.nullable = nullable;
    this.remarks = remarks;
    this.columnDef = columnDef;
    this.sqlDataType = sqlDataType;
    this.sqlDateTimeSub = sqlDateTimeSub;
    this.charOctetLength = charOctetLength;
    this.ordinalPosition = ordinalPosition;
    this.isNullable = isNullable;
    this.scopeSchema = scopeSchema;
    this.scopeTable = scopeTable;
    this.sourceDataType = sourceDataType;
    this.isAutoIncrement = isAutoIncrement;
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

  public Integer getDataType() {
    return dataType;
  }

  public String getTypeName() {
    return typeName;
  }

  public Integer getColumnSize() {
    return columnSize;
  }

  public Integer getBufferLength() {
    return bufferLength;
  }

  public Integer getDecimalDigits() {
    return decimalDigits;
  }

  public Integer getNumPrecRadix() {
    return numPrecRadix;
  }

  public Integer getNullable() {
    return nullable;
  }

  public String getRemarks() {
    return remarks;
  }

  public String getColumnDef() {
    return columnDef;
  }

  public Integer getSqlDataType() {
    return sqlDataType;
  }

  public Integer getSqlDateTimeSub() {
    return sqlDateTimeSub;
  }

  public Integer getCharOctetLength() {
    return charOctetLength;
  }

  public Integer getOrdinalPosition() {
    return ordinalPosition;
  }

  public String getIsNullable() {
    return isNullable;
  }

  public String getScopeSchema() {
    return scopeSchema;
  }

  public String getScopeTable() {
    return scopeTable;
  }

  public Short getSourceDataType() {
    return sourceDataType;
  }

  public String getIsAutoIncrement() {
    return isAutoIncrement;
  }
}
