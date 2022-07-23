package com.baremaps.database.metadata;

import java.util.List;

public record TableMetaData(Table table, List<PrimaryKey> primaryKeys, List<Column> columns) {

  public List<Column> getGeometryColumns() {
    return columns.stream().filter(column -> "geometry".equals(column.typeName())).toList();
  }
}
