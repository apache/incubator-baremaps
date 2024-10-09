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

  public GeoParquetGroup(GroupType schema, GeoParquetMetadata metadata, Schema geoParquetSchema) {
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
    Object value = data[fieldIndex];
    if (value instanceof List<?>list) {
      return list.size();
    } else {
      return value == null ? 0 : 1;
    }
  }

  private Object getValue(int fieldIndex, int index) {
    Object value = data[fieldIndex];
    if (value instanceof List<?>list) {
      return list.get(index);
    } else if (index == 0) {
      return value;
    } else {
      throw createGeoParquetException(fieldIndex, "element number " + index);
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
      return List.of(getValue(fieldIndex));
    }
    return (List<Object>) data[fieldIndex];
  }

  private GeoParquetGroup getGroup(int fieldIndex) {
    return (GeoParquetGroup) data[fieldIndex];
  }

  private void addValue(int fieldIndex, Object value) {
    Object currentValue = data[fieldIndex];
    if (currentValue instanceof List<?>) {
      ((List<Object>) currentValue).add(value);
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
    addValue(fieldIndex, value);
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
    add(getParquetSchema().getFieldIndex(field), geometry);
  }

  private GeoParquetException createGeoParquetException(int fieldIndex, String elementText) {
    String msg = String.format("Not found %d (%s) %s in group%n%s", fieldIndex,
        schema.getFieldName(fieldIndex), elementText, this);
    return new GeoParquetException(msg);
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
        if (object instanceof List<?>values) {
          for (Object value : values) {
            builder.append(indent).append(name);
            if (value == null) {
              builder.append(": NULL\n");
            } else if (value instanceof GeoParquetGroup group) {
              builder.append('\n');
              group.appendToString(builder, indent + "  ");
            } else {
              builder.append(": ").append(value).append('\n');
            }
          }
        } else {
          builder.append(indent).append(name);
          if (object instanceof GeoParquetGroup group) {
            builder.append('\n');
            group.appendToString(builder, indent + "  ");
          } else {
            builder.append(": ").append(object).append('\n');
          }
        }
      }
    }
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
    Field field = geoParquetSchema.fields().get(fieldIndex);
    if (field instanceof EnvelopeField envelopeField) {
      return new GeoParquetGroup(schema.getType(fieldIndex).asGroupType(), metadata,
          envelopeField.schema());
    } else if (field instanceof GroupField groupField) {
      return new GeoParquetGroup(schema.getType(fieldIndex).asGroupType(), metadata,
          groupField.schema());
    }
    throw new GeoParquetException("Field at index " + fieldIndex + " is not a group");
  }

  // Getter methods for different data types
  public String getString(int fieldIndex, int index) {
    return getBinaryValue(fieldIndex, index).toStringUsingUTF8();
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

  public Binary getBinaryValue(int fieldIndex, int index) {
    return (Binary) getValue(fieldIndex, index);
  }

  public Geometry getGeometry(int fieldIndex, int index) {
    byte[] bytes = getBinaryValue(fieldIndex, index).getBytes();
    try {
      return new WKBReader().read(bytes);
    } catch (ParseException e) {
      throw new GeoParquetException("WKBReader failed to parse", e);
    }
  }

  // Simplify getter methods for single values
  public Binary getBinaryValue(int fieldIndex) {
    return (Binary) getValue(fieldIndex);
  }

  public Boolean getBooleanValue(int fieldIndex) {
    return (Boolean) getValue(fieldIndex);
  }

  public Double getDoubleValue(int fieldIndex) {
    return (Double) getValue(fieldIndex);
  }

  public Float getFloatValue(int fieldIndex) {
    return (Float) getValue(fieldIndex);
  }

  public Integer getIntegerValue(int fieldIndex) {
    return (Integer) getValue(fieldIndex);
  }

  public Long getLongValue(int fieldIndex) {
    return (Long) getValue(fieldIndex);
  }

  public String getStringValue(int fieldIndex) {
    return getString(fieldIndex, 0);
  }

  public Geometry getGeometryValue(int fieldIndex) {
    return getGeometry(fieldIndex, 0);
  }

  public GeoParquetGroup getGroupValue(int fieldIndex) {
    return (GeoParquetGroup) getValue(fieldIndex);
  }

  // Simplify getter methods for list of values
  private <T> List<T> getValuesOfType(int fieldIndex, Class<T> clazz) {
    return getValues(fieldIndex).stream().map(clazz::cast).toList();
  }

  public List<Binary> getBinaryValues(int fieldIndex) {
    return getValuesOfType(fieldIndex, Binary.class);
  }

  public List<Boolean> getBooleanValues(int fieldIndex) {
    return getValuesOfType(fieldIndex, Boolean.class);
  }

  public List<Double> getDoubleValues(int fieldIndex) {
    return getValuesOfType(fieldIndex, Double.class);
  }

  public List<Float> getFloatValues(int fieldIndex) {
    return getValuesOfType(fieldIndex, Float.class);
  }

  public List<Integer> getIntegerValues(int fieldIndex) {
    return getValuesOfType(fieldIndex, Integer.class);
  }

  public List<Long> getLongValues(int fieldIndex) {
    return getValuesOfType(fieldIndex, Long.class);
  }

  public List<String> getStringValues(int fieldIndex) {
    return getValues(fieldIndex).stream()
        .map(value -> ((Binary) value).toStringUsingUTF8())
        .toList();
  }

  public List<Geometry> getGeometryValues(int fieldIndex) {
    return getValues(fieldIndex).stream()
        .map(value -> {
          try {
            return new WKBReader().read(((Binary) value).getBytes());
          } catch (ParseException e) {
            throw new GeoParquetException("WKBReader failed to parse.", e);
          }
        })
        .toList();
  }

  public List<GeoParquetGroup> getGroupValues(int fieldIndex) {
    if (data[fieldIndex] instanceof List<?> list) {
      return (List<GeoParquetGroup>) list;
    } else {
      return List.of((GeoParquetGroup) data[fieldIndex]);
    }
  }

  // Helper method to get numeric value (float or double)
  private double getNumericValue(GeoParquetGroup group, int fieldIndex) {
    Type fieldType = group.getGeoParquetSchema().fields().get(fieldIndex).type();
    return switch (fieldType) {
      case FLOAT -> group.getFloatValue(fieldIndex);
      case DOUBLE -> group.getDoubleValue(fieldIndex);
      default -> throw new GeoParquetException("Expected numeric field at index " + fieldIndex);
    };
  }

  public Envelope getEnvelopeValue(int fieldIndex) {
    return getEnvelopeValues(fieldIndex).get(0);
  }

  public List<Envelope> getEnvelopeValues(int fieldIndex) {
    return getGroupValues(fieldIndex).stream().map(group -> {
      double xMin = getNumericValue(group, 0);
      double yMin = getNumericValue(group, 1);
      double xMax = getNumericValue(group, 2);
      double yMax = getNumericValue(group, 3);
      return new Envelope(xMin, xMax, yMin, yMax);
    }).toList();
  }

  // Methods to access fields by name
  public Binary getBinaryValue(String fieldName) {
    return getBinaryValue(schema.getFieldIndex(fieldName));
  }

  public List<Binary> getBinaryValues(String fieldName) {
    return getBinaryValues(schema.getFieldIndex(fieldName));
  }

  public Boolean getBooleanValue(String fieldName) {
    return getBooleanValue(schema.getFieldIndex(fieldName));
  }

  public List<Boolean> getBooleanValues(String fieldName) {
    return getBooleanValues(schema.getFieldIndex(fieldName));
  }

  public Double getDoubleValue(String fieldName) {
    return getDoubleValue(schema.getFieldIndex(fieldName));
  }

  public List<Double> getDoubleValues(String fieldName) {
    return getDoubleValues(schema.getFieldIndex(fieldName));
  }

  public Float getFloatValue(String fieldName) {
    return getFloatValue(schema.getFieldIndex(fieldName));
  }

  public List<Float> getFloatValues(String fieldName) {
    return getFloatValues(schema.getFieldIndex(fieldName));
  }

  public Integer getIntegerValue(String fieldName) {
    return getIntegerValue(schema.getFieldIndex(fieldName));
  }

  public List<Integer> getIntegerValues(String fieldName) {
    return getIntegerValues(schema.getFieldIndex(fieldName));
  }

  public Long getLongValue(String fieldName) {
    return getLongValue(schema.getFieldIndex(fieldName));
  }

  public List<Long> getLongValues(String fieldName) {
    return getLongValues(schema.getFieldIndex(fieldName));
  }

  public String getStringValue(String fieldName) {
    return getStringValue(schema.getFieldIndex(fieldName));
  }

  public List<String> getStringValues(String fieldName) {
    return getStringValues(schema.getFieldIndex(fieldName));
  }

  public Geometry getGeometryValue(String fieldName) {
    return getGeometryValue(schema.getFieldIndex(fieldName));
  }

  public List<Geometry> getGeometryValues(String fieldName) {
    return getGeometryValues(schema.getFieldIndex(fieldName));
  }

  public GeoParquetGroup getGroupValue(String fieldName) {
    return getGroupValue(schema.getFieldIndex(fieldName));
  }

  public List<GeoParquetGroup> getGroupValues(String fieldName) {
    return getGroupValues(schema.getFieldIndex(fieldName));
  }

  public Envelope getEnvelopeValue(String fieldName) {
    return getEnvelopeValue(schema.getFieldIndex(fieldName));
  }

  public List<Envelope> getEnvelopeValues(String fieldName) {
    return getEnvelopeValues(schema.getFieldIndex(fieldName));
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
