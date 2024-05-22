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

package org.apache.baremaps.geoparquet.data;

import java.util.ArrayList;
import java.util.List;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.RecordConsumer;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.Type;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

public class GeoParquetGroup {

  private final GroupType groupType;

  private final List<Object>[] data;

  @SuppressWarnings("unchecked")
  public GeoParquetGroup(GroupType groupType) {
    this.groupType = groupType;
    this.data = new List[groupType.getFields().size()];
    for (int i = 0; i < groupType.getFieldCount(); i++) {
      this.data[i] = new ArrayList<>();
    }
  }

  public GroupType getGroupType() {
    return groupType;
  }

  public GeoParquetGroup addGroup(int fieldIndex) {
    GeoParquetGroup g = new GeoParquetGroup(groupType.getType(fieldIndex).asGroupType());
    add(fieldIndex, g);
    return g;
  }

  public GeoParquetGroup addGroup(String field) {
    return addGroup(getGroupType().getFieldIndex(field));
  }

  public GeoParquetGroup getGroup(int fieldIndex, int index) {
    return (GeoParquetGroup) getValue(fieldIndex, index);
  }

  public GeoParquetGroup getGroup(String field, int index) {
    return getGroup(getGroupType().getFieldIndex(field), index);
  }

  private Object getValue(int fieldIndex, int index) {
    List<Object> list;
    try {
      list = data[fieldIndex];
    } catch (IndexOutOfBoundsException e) {
      throw new RuntimeException(
          "not found " + fieldIndex + "(" + groupType.getFieldName(fieldIndex)
              + ") in group:\n" + this);
    }
    try {
      return list.get(index);
    } catch (IndexOutOfBoundsException e) {
      throw new RuntimeException(
          "not found " + fieldIndex + "(" + groupType.getFieldName(fieldIndex)
              + ") element number " + index + " in group:\n" + this);
    }
  }

  public int getFieldRepetitionCount(int fieldIndex) {
    List<Object> list = data[fieldIndex];
    return list == null ? 0 : list.size();
  }

  public String getValueToString(int fieldIndex, int index) {
    return String.valueOf(getValue(fieldIndex, index));
  }

  public String getString(int fieldIndex, int index) {
    return ((BinaryValue) getValue(fieldIndex, index)).getString();
  }

  public int getInteger(int fieldIndex, int index) {
    return ((IntegerValue) getValue(fieldIndex, index)).getInteger();
  }

  public long getLong(int fieldIndex, int index) {
    return ((LongValue) getValue(fieldIndex, index)).getLong();
  }

  public double getDouble(int fieldIndex, int index) {
    return ((DoubleValue) getValue(fieldIndex, index)).getDouble();
  }

  public float getFloat(int fieldIndex, int index) {
    return ((FloatValue) getValue(fieldIndex, index)).getFloat();
  }

  public boolean getBoolean(int fieldIndex, int index) {
    return ((BooleanValue) getValue(fieldIndex, index)).getBoolean();
  }

  public Binary getBinary(int fieldIndex, int index) {
    return ((BinaryValue) getValue(fieldIndex, index)).getBinary();
  }

  public NanoTime getTimeNanos(int fieldIndex, int index) {
    return NanoTime.fromInt96((Int96Value) getValue(fieldIndex, index));
  }

  public Binary getInt96(int fieldIndex, int index) {
    return ((Int96Value) getValue(fieldIndex, index)).getInt96();
  }

  public Geometry getGeometry(int fieldIndex, int index) {
    byte[] bytes = ((BinaryValue) getValue(fieldIndex, index)).getBinary().getBytes();
    try {
      return new WKBReader().read(bytes);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  private void add(int fieldIndex, Primitive value) {
    Type type = groupType.getType(fieldIndex);
    List<Object> list = data[fieldIndex];
    if (!type.isRepetition(Type.Repetition.REPEATED)
        && !list.isEmpty()) {
      throw new IllegalStateException("field " + fieldIndex + " (" + type.getName()
          + ") can not have more than one value: " + list);
    }
    list.add(value);
  }

  public void add(int fieldIndex, int value) {
    add(fieldIndex, new IntegerValue(value));
  }

  public void add(int fieldIndex, long value) {
    add(fieldIndex, new LongValue(value));
  }

  public void add(int fieldIndex, String value) {
    add(fieldIndex, new BinaryValue(Binary.fromString(value)));
  }

  public void add(int fieldIndex, NanoTime value) {
    add(fieldIndex, value.toInt96());
  }

  public void add(int fieldIndex, boolean value) {
    add(fieldIndex, new BooleanValue(value));
  }

  public void add(int fieldIndex, Binary value) {
    switch (getGroupType().getType(fieldIndex).asPrimitiveType().getPrimitiveTypeName()) {
      case BINARY:
      case FIXED_LEN_BYTE_ARRAY:
        add(fieldIndex, new BinaryValue(value));
        break;
      case INT96:
        add(fieldIndex, new Int96Value(value));
        break;
      default:
        throw new UnsupportedOperationException(
            getGroupType().asPrimitiveType().getName() + " not supported for Binary");
    }
  }

  public void add(int fieldIndex, float value) {
    add(fieldIndex, new FloatValue(value));
  }

  public void add(int fieldIndex, double value) {
    add(fieldIndex, new DoubleValue(value));
  }

  public void add(int fieldIndex, GeoParquetGroup value) {
    data[fieldIndex].add(value);
  }

  public void add(int fieldIndex, Geometry geometry) {
    byte[] bytes = new WKBWriter().write(geometry);
    add(fieldIndex, Binary.fromConstantByteArray(bytes));
  }

  public void add(String field, int value) {
    add(getGroupType().getFieldIndex(field), value);
  }

  public void add(String field, long value) {
    add(getGroupType().getFieldIndex(field), value);
  }

  public void add(String field, float value) {
    add(getGroupType().getFieldIndex(field), value);
  }

  public void add(String field, double value) {
    add(getGroupType().getFieldIndex(field), value);
  }

  public void add(String field, String value) {
    add(getGroupType().getFieldIndex(field), value);
  }

  public void add(String field, NanoTime value) {
    add(getGroupType().getFieldIndex(field), value);
  }

  public void add(String field, boolean value) {
    add(getGroupType().getFieldIndex(field), value);
  }

  public void add(String field, Binary value) {
    add(getGroupType().getFieldIndex(field), value);
  }

  public void add(String field, GeoParquetGroup value) {
    add(getGroupType().getFieldIndex(field), value);
  }

  public void add(String field, Geometry geometry) {
    byte[] bytes = new WKBWriter().write(geometry);
    add(getGroupType().getFieldIndex(field), Binary.fromConstantByteArray(bytes));
  }

  public void writeValue(int field, int index, RecordConsumer recordConsumer) {
    ((Primitive) getValue(field, index)).writeValue(recordConsumer);
  }

  @Override
  public String toString() {
    return toString("");
  }

  private void appendToString(StringBuilder builder, String indent) {
    int i = 0;
    for (Type field : groupType.getFields()) {
      String name = field.getName();
      List<Object> values = data[i];
      ++i;
      if (values != null && !values.isEmpty()) {
        for (Object value : values) {
          builder.append(indent).append(name);
          if (value == null) {
            builder.append(": NULL\n");
          } else if (value instanceof GeoParquetGroup) {
            builder.append('\n');
            ((GeoParquetGroup) value).appendToString(builder, indent + "  ");
          } else {
            builder.append(": ").append(value.toString()).append('\n');
          }
        }
      }
    }
  }

  public String toString(String indent) {
    StringBuilder builder = new StringBuilder();
    appendToString(builder, indent);
    return builder.toString();
  }

}
