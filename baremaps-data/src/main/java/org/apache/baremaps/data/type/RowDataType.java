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
import org.apache.baremaps.data.schema.DataColumn;
import org.apache.baremaps.data.schema.DataColumn.Type;
import org.apache.baremaps.data.schema.DataRow;
import org.apache.baremaps.data.schema.DataRowImpl;
import org.apache.baremaps.data.schema.DataRowType;
import org.apache.baremaps.data.type.geometry.*;

/**
 * A data type for rows.
 */
public class RowDataType implements DataType<DataRow> {

  private static final EnumMap<Type, DataType> types = new EnumMap<>(Type.class);

  static {
    types.put(Type.BYTE, new ByteDataType());
    types.put(Type.BOOLEAN, new BooleanDataType());
    types.put(Type.SHORT, new ShortDataType());
    types.put(Type.INTEGER, new IntegerDataType());
    types.put(Type.LONG, new LongDataType());
    types.put(Type.FLOAT, new FloatDataType());
    types.put(Type.DOUBLE, new DoubleDataType());
    types.put(Type.STRING, new StringDataType());
    types.put(Type.GEOMETRY, new GeometryDataType());
    types.put(Type.POINT, new PointDataType());
    types.put(Type.LINESTRING, new LineStringDataType());
    types.put(Type.POLYGON, new PolygonDataType());
    types.put(Type.MULTIPOINT, new MultiPointDataType());
    types.put(Type.MULTILINESTRING, new MultiLineStringDataType());
    types.put(Type.MULTIPOLYGON, new MultiPolygonDataType());
    types.put(Type.GEOMETRYCOLLECTION, new GeometryCollectionDataType());
    types.put(Type.COORDINATE, new CoordinateDataType());
  }

  private final DataRowType rowType;

  public RowDataType(DataRowType rowType) {
    this.rowType = rowType;
  }

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

  @Override
  public int size(final ByteBuffer buffer, final int position) {
    return buffer.getInt(position);
  }

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

  @Override
  public DataRow read(final ByteBuffer buffer, final int position) {
    int p = position + Integer.BYTES;
    var columns = rowType.columns();
    var values = new ArrayList();
    for (DataColumn column : columns) {
      var columnType = column.type();
      var dataType = types.get(columnType);
      values.add(dataType.read(buffer, p));
      p += dataType.size(buffer, p);
    }
    return new DataRowImpl(rowType, values);
  }
}
