/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.data.calcite;

import org.apache.baremaps.data.storage.DataColumn;
import org.apache.baremaps.data.storage.DataTable;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;

/**
 * A simple table based on a list.
 */
public class SqlDataTable extends AbstractTable implements ScannableTable {

  private final DataTable table;

  private RelDataType rowType;

  public SqlDataTable(DataTable table) {
    this.table = table;
  }

  @Override
  public Enumerable<Object[]> scan(final DataContext root) {
    return Linq4j.asEnumerable(() -> table.stream()
        .map(row -> row.values().toArray())
        .iterator());
  }

  @Override
  public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
    if (rowType == null) {
      rowType = createRowType(typeFactory);
    }
    return rowType;
  }

  private RelDataType createRowType(RelDataTypeFactory typeFactory) {
    var rowTypeBuilder = new RelDataTypeFactory.Builder(typeFactory);
    for (DataColumn column : table.schema().columns()) {
      rowTypeBuilder.add(column.name(), SqlTypeConversion.types.get(column.type()));
    }
    return rowTypeBuilder.build();
  }
}
