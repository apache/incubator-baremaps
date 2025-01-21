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

package org.apache.baremaps.data.type;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import org.apache.baremaps.store.DataColumn;
import org.apache.baremaps.store.DataColumn.ColumnType;
import org.apache.baremaps.store.DataRow;
import org.apache.baremaps.store.DataRowImpl;
import org.apache.baremaps.store.DataSchema;

/**
 * A {@link DataType} for reading and writing {@link DataRow} objects in {@link ByteBuffer}s.
 */
public class DataTypeImpl implements DataType<DataRow> {

  /**
   * Map of data types for each of the supported column types.
   */
  private static final EnumMap<ColumnType, DataType> types = new EnumMap<>(ColumnType.class);

  static {
    types.put(ColumnType.BYTE, new ByteDataType());
    types.put(ColumnType.BOOLEAN, new BooleanDataType());
    types.put(ColumnType.SHORT, new ShortDataType());
    types.put(ColumnType.INTEGER, new IntegerDataType());
    types.put(ColumnType.LONG, new LongDataType());
    types.put(ColumnType.FLOAT, new FloatDataType());
    types.put(ColumnType.DOUBLE, new DoubleDataType());
    types.put(ColumnType.STRING, new StringDataType());
    types.put(ColumnType.GEOMETRY, new GeometryDataType());
    types.put(ColumnType.POINT, new PointDataType());
    types.put(ColumnType.LINESTRING, new LineStringDataType());
    types.put(ColumnType.POLYGON, new PolygonDataType());
    types.put(ColumnType.MULTIPOINT, new MultiPointDataType());
    types.put(ColumnType.MULTILINESTRING, new MultiLineStringDataType());
    types.put(ColumnType.MULTIPOLYGON, new MultiPolygonDataType());
    types.put(ColumnType.GEOMETRYCOLLECTION, new GeometryCollectionDataType());
    types.put(ColumnType.COORDINATE, new CoordinateDataType());
  }

  private final DataSchema rowType;

  public DataTypeImpl(DataSchema rowType) {
    this.rowType = rowType;
  }

  /** {@inheritDoc} */
  @Override
  public int size(final DataRow row) {
    int size = Integer.BYTES;
    var columns = rowType.columns();
    for (int i = 0; i < columns.size(); i++) {
      var columnType = columns.get(i).type();
      var dataType = types.get(columnType);
      var value = row.get(i);
      size += dataType.size(value);
    }
    return size;
  }

  /** {@inheritDoc} */
  @Override
  public int size(final ByteBuffer buffer, final int position) {
    return buffer.getInt(position);
  }

  /** {@inheritDoc} */
  @Override
  public void write(final ByteBuffer buffer, final int position, final DataRow row) {
    int p = position + Integer.BYTES;
    var columns = rowType.columns();
    for (int i = 0; i < columns.size(); i++) {
      var column = columns.get(i);
      var columnType = column.type();
      var dataType = types.get(columnType);
      var value = row.get(i);
      dataType.write(buffer, p, value);
      p += dataType.size(buffer, p);
    }
    buffer.putInt(position, p - position);
  }

  /** {@inheritDoc} */
  @Override
  public DataRow read(final ByteBuffer buffer, final int position) {
    int p = position + Integer.BYTES;
    var columns = rowType.columns();
    var values = new ArrayList<>();
    for (DataColumn column : columns) {
      var columnType = column.type();
      var dataType = types.get(columnType);
      values.add(dataType.read(buffer, p));
      p += dataType.size(buffer, p);
    }
    return new DataRowImpl(rowType, values);
  }
}
