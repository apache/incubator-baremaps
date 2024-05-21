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

public class FeatureGroup {

  private final GeoParquetFileInfo fileInfo;
  private final GroupType schema;
  private final List<Object>[] data;

  @SuppressWarnings("unchecked")
  public FeatureGroup(GeoParquetFileInfo fileInfo, GroupType schema) {
    this.fileInfo = fileInfo;
    this.schema = schema;
    this.data = new List[schema.getFields().size()];
    for (int i = 0; i < schema.getFieldCount(); i++) {
      this.data[i] = new ArrayList<>();
    }
  }

  @Override
  public String toString() {
    return toString("");
  }

  private StringBuilder appendToString(StringBuilder builder, String indent) {
    int i = 0;
    for (Type field : schema.getFields()) {
      String name = field.getName();
      List<Object> values = data[i];
      ++i;
      if (values != null && !values.isEmpty()) {
        for (Object value : values) {
          builder.append(indent).append(name);
          if (value == null) {
            builder.append(": NULL\n");
          } else if (value instanceof FeatureGroup) {
            builder.append('\n');
            ((FeatureGroup) value).appendToString(builder, indent + "  ");
          } else {
            builder.append(": ").append(value.toString()).append('\n');
          }
        }
      }
    }
    return builder;
  }

  public String toString(String indent) {
    StringBuilder builder = new StringBuilder();
    appendToString(builder, indent);
    return builder.toString();
  }

  public FeatureGroup addGroup(int fieldIndex) {
    FeatureGroup g = new FeatureGroup(fileInfo, schema.getType(fieldIndex).asGroupType());
    add(fieldIndex, g);
    return g;
  }

  public FeatureGroup getGroup(int fieldIndex, int index) {
    return (FeatureGroup) getValue(fieldIndex, index);
  }

  private Object getValue(int fieldIndex, int index) {
    List<Object> list;
    try {
      list = data[fieldIndex];
    } catch (IndexOutOfBoundsException e) {
      throw new RuntimeException("not found " + fieldIndex + "(" + schema.getFieldName(fieldIndex)
          + ") in group:\n" + this);
    }
    try {
      return list.get(index);
    } catch (IndexOutOfBoundsException e) {
      throw new RuntimeException("not found " + fieldIndex + "(" + schema.getFieldName(fieldIndex)
          + ") element number " + index + " in group:\n" + this);
    }
  }

  private void add(int fieldIndex, Primitive value) {
    Type type = schema.getType(fieldIndex);
    List<Object> list = data[fieldIndex];
    if (!type.isRepetition(Type.Repetition.REPEATED)
        && !list.isEmpty()) {
      throw new IllegalStateException("field " + fieldIndex + " (" + type.getName()
          + ") can not have more than one value: " + list);
    }
    list.add(value);
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
    switch (getType().getType(fieldIndex).asPrimitiveType().getPrimitiveTypeName()) {
      case BINARY:
      case FIXED_LEN_BYTE_ARRAY:
        String fieldName = schema.getFieldName(fieldIndex);
        if (fileInfo.geometryColumns().contains(fieldName)) {
          add(fieldIndex, new GeometryValue(value));
        } else {
          add(fieldIndex, new BinaryValue(value));
        }
        break;
      case INT96:
        add(fieldIndex, new Int96Value(value));
        break;
      default:
        throw new UnsupportedOperationException(
            getType().asPrimitiveType().getName() + " not supported for Binary");
    }
  }

  public void add(int fieldIndex, float value) {
    add(fieldIndex, new FloatValue(value));
  }

  public void add(int fieldIndex, double value) {
    add(fieldIndex, new DoubleValue(value));
  }

  public void add(int fieldIndex, FeatureGroup value) {
    data[fieldIndex].add(value);
  }

  public GroupType getType() {
    return schema;
  }

  public void writeValue(int field, int index, RecordConsumer recordConsumer) {
    ((Primitive) getValue(field, index)).writeValue(recordConsumer);
  }

  public FeatureGroup addGroup(String field) {
    return addGroup(getType().getFieldIndex(field));
  }

  public FeatureGroup getGroup(String field, int index) {
    return getGroup(getType().getFieldIndex(field), index);
  }

  public FeatureGroup append(String fieldName, int value) {
    add(fieldName, value);
    return this;
  }

  public FeatureGroup append(String fieldName, float value) {
    add(fieldName, value);
    return this;
  }

  public FeatureGroup append(String fieldName, double value) {
    add(fieldName, value);
    return this;
  }

  public FeatureGroup append(String fieldName, long value) {
    add(fieldName, value);
    return this;
  }

  public FeatureGroup append(String fieldName, NanoTime value) {
    add(fieldName, value);
    return this;
  }

  public FeatureGroup append(String fieldName, String value) {
    add(fieldName, Binary.fromString(value));
    return this;
  }

  public FeatureGroup append(String fieldName, boolean value) {
    add(fieldName, value);
    return this;
  }

  public FeatureGroup append(String fieldName, Binary value) {
    add(fieldName, value);
    return this;
  }

  public void add(String field, int value) {
    add(getType().getFieldIndex(field), value);
  }

  public void add(String field, long value) {
    add(getType().getFieldIndex(field), value);
  }

  public void add(String field, float value) {
    add(getType().getFieldIndex(field), value);
  }

  public void add(String field, double value) {
    add(getType().getFieldIndex(field), value);
  }

  public void add(String field, String value) {
    add(getType().getFieldIndex(field), value);
  }

  public void add(String field, NanoTime value) {
    add(getType().getFieldIndex(field), value);
  }

  public void add(String field, boolean value) {
    add(getType().getFieldIndex(field), value);
  }

  public void add(String field, Binary value) {
    add(getType().getFieldIndex(field), value);
  }

  public void add(String field, FeatureGroup value) {
    add(getType().getFieldIndex(field), value);
  }

}
