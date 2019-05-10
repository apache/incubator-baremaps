package io.gazetteer.boilerplate.postgis;

public class StatementColumn {

  private final String catalogName;

  private final String columnClassName;

  private final int columnDisplaySize;

  private final String columnLabel;

  private final String columnName;

  private final int columnType;

  private final String columnTypeName;

  private final int precision;

  private final String schemaName;

  private final String tableName;

  private final boolean isAutoIncrement;

  private final boolean isCaseSensitive;

  private final boolean isCurrency;

  private final boolean isDefinitelyWritable;

  private final int isNullable;

  private final boolean isReadOnly;

  private final boolean isSearchable;

  private final boolean isSigned;

  private final boolean isWritable;

  public StatementColumn(String catalogName, String columnClassName, int columnDisplaySize, String columnLabel,
      String columnName, int columnType, String columnTypeName, int precision, String schemaName, String tableName, boolean isAutoIncrement,
      boolean isCaseSensitive, boolean isCurrency, boolean isDefinitelyWritable, int isNullable, boolean isReadOnly, boolean isSearchable,
      boolean isSigned, boolean isWritable) {
    this.catalogName = catalogName;
    this.columnClassName = columnClassName;
    this.columnDisplaySize = columnDisplaySize;
    this.columnLabel = columnLabel;
    this.columnName = columnName;
    this.columnType = columnType;
    this.columnTypeName = columnTypeName;
    this.precision = precision;
    this.schemaName = schemaName;
    this.tableName = tableName;
    this.isAutoIncrement = isAutoIncrement;
    this.isCaseSensitive = isCaseSensitive;
    this.isCurrency = isCurrency;
    this.isDefinitelyWritable = isDefinitelyWritable;
    this.isNullable = isNullable;
    this.isReadOnly = isReadOnly;
    this.isSearchable = isSearchable;
    this.isSigned = isSigned;
    this.isWritable = isWritable;
  }

  public String getCatalogName() {
    return catalogName;
  }

  public String getColumnClassName() {
    return columnClassName;
  }

  public int getColumnDisplaySize() {
    return columnDisplaySize;
  }

  public String getColumnLabel() {
    return columnLabel;
  }

  public String getColumnName() {
    return columnName;
  }

  public int getColumnType() {
    return columnType;
  }

  public String getColumnTypeName() {
    return columnTypeName;
  }

  public int getPrecision() {
    return precision;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public String getTableName() {
    return tableName;
  }

  public boolean isAutoIncrement() {
    return isAutoIncrement;
  }

  public boolean isCaseSensitive() {
    return isCaseSensitive;
  }

  public boolean isCurrency() {
    return isCurrency;
  }

  public boolean isDefinitelyWritable() {
    return isDefinitelyWritable;
  }

  public int getIsNullable() {
    return isNullable;
  }

  public boolean isReadOnly() {
    return isReadOnly;
  }

  public boolean isSearchable() {
    return isSearchable;
  }

  public boolean isSigned() {
    return isSigned;
  }

  public boolean isWritable() {
    return isWritable;
  }
}
