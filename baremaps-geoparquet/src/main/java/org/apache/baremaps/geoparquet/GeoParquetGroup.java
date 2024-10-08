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

package org.apache.baremaps.geoparquet;

import java.util.ArrayList;
import java.util.List;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.schema.GroupType;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

public class GeoParquetGroup {

  private final GroupType schema;

  private final GeoParquetMetadata metadata;

  private final Schema geoParquetSchema;

  private final Object[] data;

  public GeoParquetGroup(
      GroupType schema,
      GeoParquetMetadata metadata,
      Schema geoParquetSchema) {
    this.schema = schema;
    this.metadata = metadata;
    this.geoParquetSchema = geoParquetSchema;
    this.data = new Object[schema.getFields().size()];
    for (int i = 0; i < schema.getFieldCount(); i++) {
      Field field = geoParquetSchema.fields().get(i);
      if (field.cardinality() == Cardinality.REPEATED) {
        this.data[i] = new ArrayList<>();
      } else {
        this.data[i] = null; // For REQUIRED or OPTIONAL fields
      }
    }
  }

  public GeoParquetGroup addGroup(int fieldIndex) {
    GeoParquetGroup group = createGroup(fieldIndex);
    add(fieldIndex, group);
    return group;
  }

  public GeoParquetGroup addGroup(String field) {
    return addGroup(getParquetSchema().getFieldIndex(field));
  }

  public GeoParquetGroup getGroup(int fieldIndex, int index) {
    return (GeoParquetGroup) getValue(fieldIndex, index);
  }

  public GeoParquetGroup getGroup(String field, int index) {
    return getGroup(getParquetSchema().getFieldIndex(field), index);
  }

  public int getFieldRepetitionCount(int fieldIndex) {
    Field field = geoParquetSchema.fields().get(fieldIndex);
    if (field.cardinality() == Cardinality.REPEATED) {
      List<?> list = (List<?>) data[fieldIndex];
      return list == null ? 0 : list.size();
    } else {
      return data[fieldIndex] == null ? 0 : 1;
    }
  }

  public String getString(int fieldIndex, int index) {
    return getBinary(fieldIndex, index).toString();
  }

  public int getInteger(int fieldIndex, int index) {
    return (int) getValue(fieldIndex, index);
  }

  public long getLong(int fieldIndex, int index) {
    return (long) getValue(fieldIndex, index);
  }

  public double getDouble(int fieldIndex, int index) {
    return (double) getValue(fieldIndex, index);
  }

  public float getFloat(int fieldIndex, int index) {
    return (float) getValue(fieldIndex, index);
  }

  public boolean getBoolean(int fieldIndex, int index) {
    return (boolean) getValue(fieldIndex, index);
  }

  public Binary getBinary(int fieldIndex, int index) {
    return (Binary) getValue(fieldIndex, index);
  }

  public Binary getInt96(int fieldIndex, int index) {
    return (Binary) getValue(fieldIndex, index);
  }

  public Geometry getGeometry(int fieldIndex, int index) {
    byte[] bytes = getBinary(fieldIndex, index).getBytes();
    try {
      return new WKBReader().read(bytes);
    } catch (ParseException e) {
      throw new GeoParquetException("WKBReader failed to parse", e);
    }
  }

  private Object getValue(int fieldIndex, int index) {
    Object value = data[fieldIndex];
    if (value instanceof List<?>list) {
      return list.get(index);
    } else if (index == 0) {
      return value;
    } else {
      String elementText = String.format(" element number %d ", index);
      throw createGeoParquetException(fieldIndex, elementText);
    }
  }

  private GeoParquetException createGeoParquetException(int fieldIndex, String elementText) {
    String msg = String.format("Not found %d (%s)%s in group%n%s", fieldIndex,
        schema.getFieldName(fieldIndex), elementText, this);
    return new GeoParquetException(msg);
  }

  private void addValue(int fieldIndex, Object value) {
    if (data[fieldIndex] instanceof List list) {
      list.add(value);
    } else {
      data[fieldIndex] = value;
    }
  }

  public void add(int fieldIndex, int value) {
    addValue(fieldIndex, value);
  }

  public void add(int fieldIndex, long value) {
    addValue(fieldIndex, value);
  }

  public void add(int fieldIndex, String value) {
    addValue(fieldIndex, Binary.fromString(value));
  }

  public void add(int fieldIndex, boolean value) {
    addValue(fieldIndex, value);
  }

  public void add(int fieldIndex, Binary value) {
    switch (getParquetSchema().getType(fieldIndex).asPrimitiveType().getPrimitiveTypeName()) {
      case BINARY, FIXED_LEN_BYTE_ARRAY:
        addValue(fieldIndex, value);
        break;
      case INT96:
        addValue(fieldIndex, value);
        break;
      default:
        throw new UnsupportedOperationException(
            getParquetSchema().asPrimitiveType().getName() + " not supported for Binary");
    }
  }

  public void add(int fieldIndex, float value) {
    addValue(fieldIndex, value);
  }

  public void add(int fieldIndex, double value) {
    addValue(fieldIndex, value);
  }

  public void add(int fieldIndex, GeoParquetGroup value) {
    addValue(fieldIndex, value);
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

  public void add(String field, GeoParquetGroup value) {
    add(getParquetSchema().getFieldIndex(field), value);
  }

  public void add(String field, Geometry geometry) {
    byte[] bytes = new WKBWriter().write(geometry);
    add(getParquetSchema().getFieldIndex(field), Binary.fromConstantByteArray(bytes));
  }

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
      Object object = data[i];
      ++i;
      if (object != null) {
        if (object instanceof List values) {
          for (Object value : values) {
            builder.append(indent).append(name);
            if (value == null) {
              builder.append(": NULL\n");
            } else if (value instanceof GeoParquetGroup geoParquetGroup) {
              builder.append('\n');
              geoParquetGroup.appendToString(builder, indent + "  ");
            } else {
              builder.append(": ").append(value).append('\n');
            }
          }
        } else {
          builder.append(indent).append(name);
          if (object == null) {
            builder.append(": NULL\n");
          } else if (object instanceof GeoParquetGroup geoParquetGroup) {
            builder.append('\n');
            geoParquetGroup.appendToString(builder, indent + "  ");
          } else {
            builder.append(": ").append(object).append('\n');
          }
        }
      }
    }
  }

  private Object getValue(int fieldIndex) {
    Field field = geoParquetSchema.fields().get(fieldIndex);
    if (field.cardinality() == Cardinality.REPEATED) {
      throw new IllegalStateException("Field " + fieldIndex + " (" + field.name()
          + ") is repeated. Use getValues() instead.");
    }
    return data[fieldIndex];
  }

  public List<Object> getValues(int fieldIndex) {
    Field field = geoParquetSchema.fields().get(fieldIndex);
    if (field.cardinality() != Cardinality.REPEATED) {
      throw new IllegalStateException("Field " + fieldIndex + " (" + field.name()
          + ") is not repeated. Use getValue() instead.");
    }
    return (List<Object>) data[fieldIndex];
  }


  private List<GeoParquetGroup> getGroups(int fieldIndex) {
    return (List<GeoParquetGroup>) data[fieldIndex];
  }

  public Schema getGeoParquetSchema() {
    return geoParquetSchema;
  }

  public GroupType getParquetSchema() {
    return schema;
  }

  public GeoParquetMetadata getGeoParquetMetadata() {
    return metadata;
  }

  public GeoParquetGroup createGroup(int fieldIndex) {
    if (geoParquetSchema.fields().get(fieldIndex) instanceof EnvelopeField envelopeField) {
      return new GeoParquetGroup(schema.getType(fieldIndex).asGroupType(), metadata,
          envelopeField.schema());
    }

    if (geoParquetSchema.fields().get(fieldIndex) instanceof GroupField groupField) {
      return new GeoParquetGroup(schema.getType(fieldIndex).asGroupType(), metadata,
          groupField.schema());
    }

    GroupField field = ((GroupField) geoParquetSchema.fields().get(fieldIndex));
    return new GeoParquetGroup(
        schema.getType(fieldIndex).asGroupType(),
        metadata,
        field.schema());
  }

  public Binary getBinary(int fieldIndex) {
    return getBinary(fieldIndex, 0);
  }

  public List<Binary> getBinaryValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Binary.class::cast).toList();
  }

  public Boolean getBooleanValue(int fieldIndex) {
    return getBoolean(fieldIndex, 0);
  }

  public List<Boolean> getBooleanValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Boolean.class::cast).toList();
  }

  public Double getDoubleValue(int fieldIndex) {
    return getDouble(fieldIndex, 0);
  }

  public List<Double> getDoubleValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Double.class::cast).toList();
  }

  public Float getFloatValue(int fieldIndex) {
    return getFloat(fieldIndex, 0);
  }

  public List<Float> getFloatValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Float.class::cast).toList();
  }

  public Integer getIntegerValue(int fieldIndex) {
    return getInteger(fieldIndex, 0);
  }

  public List<Integer> getIntegerValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Integer.class::cast).toList();
  }

  public Binary getInt96Value(int fieldIndex) {
    return getBinary(fieldIndex, 0);
  }

  public List<Binary> getInt96Values(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Binary.class::cast).toList();
  }

  public Long getLongValue(int fieldIndex) {
    return getLong(fieldIndex, 0);
  }

  public List<Long> getLongValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(Long.class::cast).toList();
  }

  public String getStringValue(int fieldIndex) {
    return getString(fieldIndex, 0);
  }

  public List<String> getStringValues(int fieldIndex) {
    return getValues(fieldIndex).stream().map(String.class::cast).toList();
  }

  public Geometry getGeometryValue(int fieldIndex) {
    return getGeometry(fieldIndex, 0);
  }

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

  public Envelope getEnvelopeValue(int fieldIndex) {
    return getEnvelopeValues(fieldIndex).get(0);
  }

  public List<Envelope> getEnvelopeValues(int fieldIndex) {
    return getGroupValues(fieldIndex).stream().map(group -> {
      double xMin = group.getGeoParquetSchema().fields().get(0).type().equals(Type.FLOAT)
          ? (double) group.getFloatValue(0)
          : group.getDoubleValue(0);
      double yMin = group.getGeoParquetSchema().fields().get(1).type().equals(Type.FLOAT)
          ? (double) group.getFloatValue(1)
          : group.getDoubleValue(1);
      double xMax = group.getGeoParquetSchema().fields().get(2).type().equals(Type.FLOAT)
          ? (double) group.getFloatValue(2)
          : group.getDoubleValue(2);
      double yMax = group.getGeoParquetSchema().fields().get(0).type().equals(Type.FLOAT)
          ? (double) group.getFloatValue(3)
          : group.getDoubleValue(3);
      return new Envelope(xMin, xMax, yMin, yMax);
    }).toList();
  }

  public GeoParquetGroup getGroupValue(int fieldIndex) {
    return getGroupValues(fieldIndex).get(0);
  }

  public List<GeoParquetGroup> getGroupValues(int fieldIndex) {
    return getGroups(fieldIndex);
  }

  public Binary getBinary(String fieldName) {
    return getBinaryValues(fieldName).get(0);
  }

  public List<Binary> getBinaryValues(String fieldName) {
    return getBinaryValues(schema.getFieldIndex(fieldName));
  }

  public Boolean getBooleanValue(String fieldName) {
    return getBooleanValues(fieldName).get(0);
  }

  public List<Boolean> getBooleanValues(String fieldName) {
    return getBooleanValues(schema.getFieldIndex(fieldName));
  }

  public Double getDoubleValue(String fieldName) {
    return getDoubleValues(fieldName).get(0);
  }

  public List<Double> getDoubleValues(String fieldName) {
    return getDoubleValues(schema.getFieldIndex(fieldName));
  }

  public Float getFloatValue(String fieldName) {
    return getFloatValues(fieldName).get(0);
  }

  public List<Float> getFloatValues(String fieldName) {
    return getFloatValues(schema.getFieldIndex(fieldName));
  }

  public Integer getIntegerValue(String fieldName) {
    return getIntegerValues(fieldName).get(0);
  }

  public List<Integer> getIntegerValues(String fieldName) {
    return getIntegerValues(schema.getFieldIndex(fieldName));
  }

  public Binary getInt96Value(String fieldName) {
    return getBinaryValues(fieldName).get(0);
  }

  public List<Binary> getInt96Values(String fieldName) {
    return getBinaryValues(schema.getFieldIndex(fieldName));
  }

  public Long getLongValue(String fieldName) {
    return getLongValues(fieldName).get(0);
  }

  public List<Long> getLongValues(String fieldName) {
    return getLongValues(schema.getFieldIndex(fieldName));
  }

  public String getStringValue(String fieldName) {
    return getStringValues(fieldName).get(0);
  }

  public List<String> getStringValues(String fieldName) {
    return getStringValues(schema.getFieldIndex(fieldName));
  }

  public Geometry getGeometryValue(String fieldName) {
    return getGeometryValues(fieldName).get(0);
  }

  public List<Geometry> getGeometryValues(String fieldName) {
    return getGeometryValues(schema.getFieldIndex(fieldName));
  }

  public Envelope getEnvelopeValue(String fieldName) {
    return getEnvelopeValues(fieldName).get(0);
  }

  public List<Envelope> getEnvelopeValues(String fieldName) {
    return getEnvelopeValues(schema.getFieldIndex(fieldName));
  }

  public GeoParquetGroup getGroupValue(String fieldName) {
    return getGroupValues(fieldName).get(0);
  }

  public List<GeoParquetGroup> getGroupValues(String fieldName) {
    return getGroupValues(schema.getFieldIndex(fieldName));
  }

  /**
   * A GeoParquet schema that describes the fields of a group and can easily be introspected.
   *
   * @param name
   * @param fields the fields of the schema
   */
  public record Schema(String name, List<Field> fields) {

  }

  /**
   * A sealed inteface for the fields of a GeoParquet schema.
   * <p>
   * Sealed interfaces were introduced in Java 17 and can be used with pattern matching since Java
   * 21.
   */
  sealed public
  interface Field {
     String name();

     Type type();

     Cardinality cardinality();
  }

  record BinaryField(String name, Cardinality cardinality) implements Field {

    @Override
    public Type type() {
      return Type.BINARY;
    }
  }

  record BooleanField(String name, Cardinality cardinality) implements Field {

    @Override
    public Type type() {
      return Type.BOOLEAN;
    }
  }

  record DoubleField(String name, Cardinality cardinality) implements Field {

    @Override
    public Type type() {
      return Type.DOUBLE;
    }
  }

  record FloatField(String name, Cardinality cardinality) implements Field {

    @Override
    public Type type() {
      return Type.FLOAT;
    }
  }

  record IntegerField(String name, Cardinality cardinality) implements Field {

    @Override
    public Type type() {
      return Type.INTEGER;
    }
  }

  record Int96Field(String name, Cardinality cardinality) implements Field {

    @Override
    public Type type() {
      return Type.INT96;
    }
  }

  record LongField(String name, Cardinality cardinality) implements Field {

    @Override
    public Type type() {
      return Type.LONG;
    }
  }

  record StringField(String name, Cardinality cardinality) implements Field {

    @Override
    public Type type() {
      return Type.STRING;
    }
  }

  record GeometryField(String name, Cardinality cardinality) implements Field {

    @Override
    public Type type() {
      return Type.GEOMETRY;
    }
  }

  record EnvelopeField(String name, Cardinality cardinality, Schema schema) implements Field {

    @Override
    public Type type() {
      return Type.ENVELOPE;
    }
  }

  public record GroupField(String name, Cardinality cardinality, Schema schema) implements Field {

    @Override
    public Type type() {
      return Type.GROUP;
    }
  }

  /**
   * The type of a GeoParquet field.
   */
  public enum Type {
    BINARY,
    BOOLEAN,
    DOUBLE,
    FLOAT,
    INTEGER,
    INT96,
    LONG,
    STRING,
    GEOMETRY,
    ENVELOPE,
    GROUP
  }

  /**
   * The cardinality of a GeoParquet field.
   */
  public enum Cardinality {
    REQUIRED,
    OPTIONAL,
    REPEATED
  }

}
