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
import org.apache.baremaps.geoparquet.GeoParquetException;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.RecordConsumer;
import org.apache.parquet.schema.GroupType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

public class GeoParquetGroupImpl implements GeoParquetGroup {

  private final GroupType schema;

  private final GeoParquetMetadata metadata;

  private final Schema geoParquetSchema;

  private final List<?>[] data;

  public GeoParquetGroupImpl(
      GroupType schema,
      GeoParquetMetadata metadata,
      Schema geoParquetSchema) {
    this.schema = schema;
    this.metadata = metadata;
    this.geoParquetSchema = geoParquetSchema;
    this.data = new List[schema.getFields().size()];
    for (int i = 0; i < schema.getFieldCount(); i++) {
      this.data[i] = new ArrayList<>();
    }
  }

  public GeoParquetGroupImpl addGroup(int fieldIndex) {
    GeoParquetGroupImpl group = createGroup(fieldIndex);
    add(fieldIndex, group);
    return group;
  }

  public GeoParquetGroupImpl addGroup(String field) {
    return addGroup(getParquetSchema().getFieldIndex(field));
  }

  public GeoParquetGroupImpl getGroup(int fieldIndex, int index) {
    return (GeoParquetGroupImpl) getValue(fieldIndex, index);
  }

  public GeoParquetGroupImpl getGroup(String field, int index) {
    return getGroup(getParquetSchema().getFieldIndex(field), index);
  }

  public int getFieldRepetitionCount(int fieldIndex) {
    List<?> list = data[fieldIndex];
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

  public Binary getInt96(int fieldIndex, int index) {
    return ((Int96Value) getValue(fieldIndex, index)).getInt96();
  }

  public Geometry getGeometry(int fieldIndex, int index) {
    byte[] bytes = ((BinaryValue) getValue(fieldIndex, index)).getBinary().getBytes();
    try {
      return new WKBReader().read(bytes);
    } catch (ParseException e) {
      throw new GeoParquetException("WKBReader failed to parse", e);
    }
  }

  private Object getValue(int fieldIndex, int index) {
    List<?> list = getObjects(fieldIndex);
    try {
      return list.get(index);
    } catch (IndexOutOfBoundsException e) {
      String elementText = String.format(" element number %d ", index);
      throw createGeoParquetException(fieldIndex, elementText);
    }
  }

  private List<?> getObjects(int fieldIndex) {
    List<?> list;
    if (fieldIndex < 0 || fieldIndex >= data.length) {
      throw createGeoParquetException(fieldIndex, "");
    }
    list = data[fieldIndex];
    return list;
  }

  private GeoParquetException createGeoParquetException(int fieldIndex, String elementText) {
    String msg = String.format("Not found %d (%s)%s in group%n%s", fieldIndex,
        schema.getFieldName(fieldIndex), elementText, this);
    return new GeoParquetException(msg);
  }

  private void add(int fieldIndex, Primitive value) {
    org.apache.parquet.schema.Type type = schema.getType(fieldIndex);
    List list = getObjects(fieldIndex);
    if (!type.isRepetition(org.apache.parquet.schema.Type.Repetition.REPEATED)
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

  public void add(int fieldIndex, boolean value) {
    add(fieldIndex, new BooleanValue(value));
  }

  public void add(int fieldIndex, Binary value) {
    switch (getParquetSchema().getType(fieldIndex).asPrimitiveType().getPrimitiveTypeName()) {
      case BINARY, FIXED_LEN_BYTE_ARRAY:
        add(fieldIndex, new BinaryValue(value));
        break;
      case INT96:
        add(fieldIndex, new Int96Value(value));
        break;
      default:
        throw new UnsupportedOperationException(
            getParquetSchema().asPrimitiveType().getName() + " not supported for Binary");
    }
  }

  public void add(int fieldIndex, float value) {
    add(fieldIndex, new FloatValue(value));
  }

  public void add(int fieldIndex, double value) {
    add(fieldIndex, new DoubleValue(value));
  }

  public void add(int fieldIndex, GeoParquetGroupImpl value) {
    List list = data[fieldIndex];
    list.add(value);
  }

  public void add(int fieldIndex, Geometry geometry) {
    byte[] bytes = new WKBWriter().write(geometry);
    add(fieldIndex, Binary.fromConstantByteArray(bytes));
  }

  public void add(String field, int value) {
    add(getParquetSchema().getFieldIndex(field), value);
  }

  public void add(String field, long value) {
    add(getParquetSchema().getFieldIndex(field), value);
  }

  public void add(String field, float value) {
    add(getParquetSchema().getFieldIndex(field), value);
  }

  public void add(String field, double value) {
    add(getParquetSchema().getFieldIndex(field), value);
  }

  public void add(String field, String value) {
    add(getParquetSchema().getFieldIndex(field), value);
  }

  public void add(String field, boolean value) {
    add(getParquetSchema().getFieldIndex(field), value);
  }

  public void add(String field, Binary value) {
    add(getParquetSchema().getFieldIndex(field), value);
  }

  public void add(String field, GeoParquetGroupImpl value) {
    add(getParquetSchema().getFieldIndex(field), value);
  }

  public void add(String field, Geometry geometry) {
    byte[] bytes = new WKBWriter().write(geometry);
    add(getParquetSchema().getFieldIndex(field), Binary.fromConstantByteArray(bytes));
  }

  public void writeValue(int field, int index, RecordConsumer recordConsumer) {
    ((Primitive) getValue(field, index)).writeValue(recordConsumer);
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent) {
    StringBuilder builder = new StringBuilder();
    appendToString(builder, indent);
    return builder.toString();
  }

  private void appendToString(StringBuilder builder, String indent) {
    int i = 0;
    for (org.apache.parquet.schema.Type field : schema.getFields()) {
      String name = field.getName();
      List<?> values = data[i];
      ++i;
      if (values != null && !values.isEmpty()) {
        for (Object value : values) {
          builder.append(indent).append(name);
          if (value == null) {
            builder.append(": NULL\n");
          } else if (value instanceof GeoParquetGroupImpl geoParquetGroupImpl) {
            builder.append('\n');
            geoParquetGroupImpl.appendToString(builder, indent + "  ");
          } else {
            builder.append(": ").append(value).append('\n');
          }
        }
      }
    }
  }

  private List<Primitive> getValues(int fieldIndex) {
    return (List<Primitive>) data[fieldIndex];
  }

  private List<GeoParquetGroup> getGroups(int fieldIndex) {
    return (List<GeoParquetGroup>) data[fieldIndex];
  }

  @Override
  public Schema getSchema() {
    return geoParquetSchema;
  }

  @Override
  public GroupType getParquetSchema() {
    return schema;
  }

  @Override
  public GeoParquetMetadata getGeoParquetMetadata() {
    return metadata;
  }

  @Override
  public GeoParquetGroupImpl createGroup(int fieldIndex) {
    GroupField field = ((GroupField) geoParquetSchema.fields().get(fieldIndex));
    GeoParquetGroupImpl group =
        new GeoParquetGroupImpl(schema.getType(fieldIndex).asGroupType(), metadata, field.schema());
    return group;
  }

  @Override
  public Binary getBinaryValue(int fieldIndex) {
    return getBinaryValues(fieldIndex).get(0);
  }

  @Override
  public List<Binary> getBinaryValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Primitive::getBinary).toList();
  }

  @Override
  public Boolean getBooleanValue(int fieldIndex) {
    return getBooleanValues(fieldIndex).get(0);
  }

  @Override
  public List<Boolean> getBooleanValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Primitive::getBoolean).toList();
  }

  @Override
  public Double getDoubleValue(int fieldIndex) {
    return getDoubleValues(fieldIndex).get(0);
  }

  @Override
  public List<Double> getDoubleValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Primitive::getDouble).toList();
  }

  @Override
  public Float getFloatValue(int fieldIndex) {
    return getFloatValues(fieldIndex).get(0);
  }

  @Override
  public List<Float> getFloatValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Primitive::getFloat).toList();
  }

  @Override
  public Integer getIntegerValue(int fieldIndex) {
    return getIntegerValues(fieldIndex).get(0);
  }

  @Override
  public List<Integer> getIntegerValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Primitive::getInteger).toList();
  }

  @Override
  public Binary getInt96Value(int fieldIndex) {
    return getBinaryValues(fieldIndex).get(0);
  }

  @Override
  public List<Binary> getInt96Values(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Primitive::getBinary).toList();
  }

  @Override
  public Binary getNanoTimeValue(int fieldIndex) {
    return getBinaryValues(fieldIndex).get(0);
  }

  @Override
  public List<Binary> getNanoTimeValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Primitive::getBinary).toList();
  }

  @Override
  public Long getLongValue(int fieldIndex) {
    return getLongValues(fieldIndex).get(0);
  }

  @Override
  public List<Long> getLongValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Primitive::getLong).toList();
  }

  @Override
  public String getStringValue(int fieldIndex) {
    return getStringValues(fieldIndex).get(0);
  }

  @Override
  public List<String> getStringValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Primitive::getString).toList();
  }

  @Override
  public Geometry getGeometryValue(int fieldIndex) {
    return getGeometryValues(fieldIndex).get(0);
  }

  @Override
  public List<Geometry> getGeometryValues(int fieldIndex) {
    List<Geometry> geometries = new ArrayList<>();
    for (Binary binary : getBinaryValues(fieldIndex)) {
      try {
        geometries.add(new WKBReader().read(binary.getBytes()));
      } catch (ParseException e) {
        throw new GeoParquetException("WKBReader failed to parse.", e);
      }
    }
    return geometries;
  }

  @Override
  public GeoParquetGroup getGroupValue(int fieldIndex) {
    return getGroupValues(fieldIndex).get(0);
  }

  @Override
  public List<GeoParquetGroup> getGroupValues(int fieldIndex) {
    return getGroups(fieldIndex);
  }

  @Override
  public Binary getBinaryValue(String fieldName) {
    return getBinaryValues(fieldName).get(0);
  }

  @Override
  public List<Binary> getBinaryValues(String fieldName) {
    return getBinaryValues(schema.getFieldIndex(fieldName));
  }

  @Override
  public Boolean getBooleanValue(String fieldName) {
    return getBooleanValues(fieldName).get(0);
  }

  @Override
  public List<Boolean> getBooleanValues(String fieldName) {
    return getBooleanValues(schema.getFieldIndex(fieldName));
  }

  @Override
  public Double getDoubleValue(String fieldName) {
    return getDoubleValues(fieldName).get(0);
  }

  @Override
  public List<Double> getDoubleValues(String fieldName) {
    return getDoubleValues(schema.getFieldIndex(fieldName));
  }

  @Override
  public Float getFloatValue(String fieldName) {
    return getFloatValues(fieldName).get(0);
  }

  @Override
  public List<Float> getFloatValues(String fieldName) {
    return getFloatValues(schema.getFieldIndex(fieldName));
  }

  @Override
  public Integer getIntegerValue(String fieldName) {
    return getIntegerValues(fieldName).get(0);
  }

  @Override
  public List<Integer> getIntegerValues(String fieldName) {
    return getIntegerValues(schema.getFieldIndex(fieldName));
  }

  @Override
  public Binary getInt96Value(String fieldName) {
    return getBinaryValues(fieldName).get(0);
  }

  @Override
  public List<Binary> getInt96Values(String fieldName) {
    return getBinaryValues(schema.getFieldIndex(fieldName));
  }

  @Override
  public Binary getNanoTimeValue(String fieldName) {
    return getBinaryValues(fieldName).get(0);
  }

  @Override
  public List<Binary> getNanoTimeValues(String fieldName) {
    return getBinaryValues(schema.getFieldIndex(fieldName));
  }

  @Override
  public Long getLongValue(String fieldName) {
    return getLongValues(fieldName).get(0);
  }

  @Override
  public List<Long> getLongValues(String fieldName) {
    return getLongValues(schema.getFieldIndex(fieldName));
  }

  @Override
  public String getStringValue(String fieldName) {
    return getStringValues(fieldName).get(0);
  }

  @Override
  public List<String> getStringValues(String fieldName) {
    return getStringValues(schema.getFieldIndex(fieldName));
  }

  @Override
  public Geometry getGeometryValue(String fieldName) {
    return getGeometryValues(fieldName).get(0);
  }

  @Override
  public List<Geometry> getGeometryValues(String fieldName) {
    return getGeometryValues(schema.getFieldIndex(fieldName));
  }

  @Override
  public GeoParquetGroup getGroupValue(String fieldName) {
    return getGroupValues(fieldName).get(0);
  }

  @Override
  public List<GeoParquetGroup> getGroupValues(String fieldName) {
    return getGroupValues(schema.getFieldIndex(fieldName));
  }

  @Override
  public void setBinaryValue(int fieldIndex, Binary binaryValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBinaryValues(int fieldIndex, List<Binary> binaryValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBooleanValue(int fieldIndex, Boolean booleanValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBooleanValues(int fieldIndex, List<Boolean> booleanValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDoubleValue(int fieldIndex, Double doubleValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDoubleValues(int fieldIndex, List<Double> doubleValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFloatValue(int fieldIndex, Float floatValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFloatValues(int fieldIndex, List<Float> floatValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setIntegerValue(int fieldIndex, Integer integerValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setIntegerValues(int fieldIndex, List<Integer> integerValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setInt96Value(int fieldIndex, Binary int96Value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setInt96Values(int fieldIndex, List<Binary> int96Values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setNanoTimeValue(int fieldIndex, Binary nanoTimeValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setNanoTimeValues(int fieldIndex, List<Binary> nanoTimeValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLongValue(int fieldIndex, Long longValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLongValues(int fieldIndex, List<Long> longValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setStringValue(int fieldIndex, String stringValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setStringValues(int fieldIndex, List<String> stringValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setGeometryValue(int fieldIndex, Geometry geometryValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setGeometryValues(int fieldIndex, List<Geometry> geometryValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setGroupValue(int fieldIndex, GeoParquetGroup groupValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setGroupValues(int fieldIndex, List<GeoParquetGroup> groupValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBinaryValue(String fieldName, Binary binaryValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBinaryValues(String fieldName, List<Binary> binaryValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBooleanValue(String fieldName, Boolean booleanValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBooleanValues(String fieldName, List<Boolean> booleanValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDoubleValue(String fieldName, Double doubleValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDoubleValues(String fieldName, List<Double> doubleValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFloatValue(String fieldName, Float floatValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFloatValues(String fieldName, List<Float> floatValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setIntegerValue(String fieldName, Integer integerValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setIntegerValues(String fieldName, List<Integer> integerValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setInt96Value(String fieldName, Binary int96Value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setInt96Values(String fieldName, List<Binary> int96Values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setNanoTimeValue(String fieldName, Binary nanoTimeValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setNanoTimeValues(String fieldName, List<Binary> nanoTimeValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLongValue(String fieldName, Long longValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLongValues(String fieldName, List<Long> longValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setStringValue(String fieldName, String stringValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setStringValues(String fieldName, List<String> stringValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setGeometryValue(String fieldName, Geometry geometryValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setGeometryValues(String fieldName, List<Geometry> geometryValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setGroupValue(String fieldName, GeoParquetGroup groupValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setGroupValues(String fieldName, List<GeoParquetGroup> groupValues) {
    throw new UnsupportedOperationException();
  }

}
