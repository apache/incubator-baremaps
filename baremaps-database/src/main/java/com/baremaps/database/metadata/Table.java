package com.baremaps.database.metadata;

public record Table(
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
