package com.baremaps.database.metadata;

public record PrimaryKey(
    String tableCat,
    String tableSchem,
    String tableName,
    String columnName,
    short keySeq,
    String pkName) {

}
