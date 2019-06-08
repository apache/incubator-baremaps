package io.gazetteer.postgis.metadata;

import com.google.common.base.MoreObjects;

public final class FunctionColumn {

  private final String functionCatalog;
  private final String functionSchema;
  private final String functionName;
  private final String columnName;
  private final Short columnType;
  private final Integer dataType;
  private final String typeName;
  private final Integer precision;
  private final Integer length;
  private final Short scale;
  private final Short radix;
  private final Short nullable;
  private final String remarks;
  private final Integer charOctetLength;
  private final Integer ordinalPosition;
  private final String isNullable;
  private final String specificName;

  public FunctionColumn(
      String functionCatalog,
      String functionSchema,
      String functionName,
      String columnName,
      Short columnType,
      Integer dataType,
      String typeName,
      Integer precision,
      Integer length,
      Short scale,
      Short radix,
      Short nullable,
      String remarks,
      Integer charOctetLength,
      Integer ordinalPosition,
      String isNullable,
      String specificName) {
    this.functionCatalog = functionCatalog;
    this.functionSchema = functionSchema;
    this.functionName = functionName;
    this.columnName = columnName;
    this.columnType = columnType;
    this.dataType = dataType;
    this.typeName = typeName;
    this.precision = precision;
    this.length = length;
    this.scale = scale;
    this.radix = radix;
    this.nullable = nullable;
    this.remarks = remarks;
    this.charOctetLength = charOctetLength;
    this.ordinalPosition = ordinalPosition;
    this.isNullable = isNullable;
    this.specificName = specificName;
  }

  public String getFunctionCatalog() {
    return functionCatalog;
  }

  public String getFunctionSchema() {
    return functionSchema;
  }

  public String getColumnName() {
    return columnName;
  }

  public Short getColumnType() {
    return columnType;
  }

  public Integer getDataType() {
    return dataType;
  }

  public String getTypeName() {
    return typeName;
  }

  public Integer getPrecision() {
    return precision;
  }

  public Integer getLength() {
    return length;
  }

  public Short getScale() {
    return scale;
  }

  public Short getRadix() {
    return radix;
  }

  public Short getNullable() {
    return nullable;
  }

  public String getRemarks() {
    return remarks;
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

  public String getSpecificName() {
    return specificName;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("functionCatalog", functionCatalog)
        .add("functionSchema", functionSchema)
        .add("functionName", functionName)
        .add("columnName", columnName)
        .add("columnType", columnType)
        .add("dataType", dataType)
        .add("typeName", typeName)
        .add("precision", precision)
        .add("length", length)
        .add("scale", scale)
        .add("radix", radix)
        .add("nullable", nullable)
        .add("remarks", remarks)
        .add("charOctetLength", charOctetLength)
        .add("ordinalPosition", ordinalPosition)
        .add("isNullable", isNullable)
        .add("specificName", specificName)
        .toString();
  }
}
