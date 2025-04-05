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

package org.apache.baremaps.calcite.rpsl;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import org.apache.baremaps.calcite.DataColumn;
import org.apache.baremaps.calcite.DataRow;
import org.apache.baremaps.calcite.DataSchema;
import org.apache.baremaps.rpsl.RpslObject;
import org.apache.baremaps.rpsl.RpslReader;

/**
 * An iterator over DataRow objects created from RPSL data.
 */
class RpslDataRowIterator implements Iterator<DataRow> {

  private final Iterator<RpslObject> rpslObjectIterator;
  private final DataSchema schema;

  public RpslDataRowIterator(InputStream inputStream, DataSchema schema) {
    this.schema = schema;
    RpslReader rpslReader = new RpslReader();
    this.rpslObjectIterator = rpslReader.read(inputStream).iterator();
  }

  @Override
  public boolean hasNext() {
    return rpslObjectIterator.hasNext();
  }

  @Override
  public DataRow next() {
    RpslObject rpslObject = rpslObjectIterator.next();
    return createDataRow(rpslObject);
  }

  private DataRow createDataRow(RpslObject rpslObject) {
    DataRow dataRow = schema.createRow();

    for (DataColumn column : schema.columns()) {
      String columnName = column.name().toLowerCase();

      switch (column.cardinality()) {
        case REQUIRED:
        case OPTIONAL:
          Object value = getSingleValue(rpslObject, column);
          if (value != null) {
            dataRow.set(columnName, value);
          }
          break;
        case REPEATED:
          List<?> values = getRepeatedValue(rpslObject, column);
          if (values != null && !values.isEmpty()) {
            dataRow.set(columnName, values);
          }
          break;
      }
    }

    return dataRow;
  }

  private Object getSingleValue(RpslObject rpslObject, DataColumn column) {
    String columnName = column.name().toLowerCase();
    return rpslObject.first(columnName).orElse(null);
  }

  private List<String> getRepeatedValue(RpslObject rpslObject, DataColumn column) {
    String columnName = column.name().toLowerCase();
    return rpslObject.all(columnName);
  }
}
