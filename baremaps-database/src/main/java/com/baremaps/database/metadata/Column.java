package com.baremaps.database.metadata;

public record Column(
    String tableCat,
    String tableSchem,
    String tableName,
    String columnName,
    int dataType,
    String typeName,
    int columnSize,
    int decimalDigits,
    int numPrecRadix,
    int nullable,
    String remarks,
    String columnDef,
    int sqlDataType,
    int sqlDateTimeSub,
    int charOctetLenght,
    int ordinalPosition,
    String isNullable,
    String scopeCatalog,
    String scopeSchema,
    String scopeTable,
    short sourceDataType,
    String isAutoIncrement,
    String isGeneratedColumn) {

}
