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

import java.util.List;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.schema.GroupType;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

/**
 * A group of fields in a GeoParquet file.
 * 
 */
public interface GeoParquetGroup {

  /**
   * Returns the GeoParquet schema of the group built upon the Parquet schema and the GeoParquet
   * metadata.
   *
   * @return the GeoParquet schema
   */
  Schema getSchema();

  /**
   * Returns the Parquet schema of the group.
   *
   * @return the Parquet schema
   */
  GroupType getParquetSchema();

  /**
   * Returns the GeoParquet metadata of the group.
   *
   * @return the Parquet metadata
   */
  GeoParquetMetadata getGeoParquetMetadata();

  /**
   * Creates a new empty group in the group at the specified field index.
   *
   * @param fieldIndex the field index
   * @return the new group
   */
  GeoParquetGroup createGroup(int fieldIndex);

  List<Primitive> getValues(int fieldIndex);

  Binary getBinaryValue(int fieldIndex);

  List<Binary> getBinaryValues(int fieldIndex);

  Boolean getBooleanValue(int fieldIndex);

  List<Boolean> getBooleanValues(int fieldIndex);

  Double getDoubleValue(int fieldIndex);

  List<Double> getDoubleValues(int fieldIndex);

  Float getFloatValue(int fieldIndex);

  List<Float> getFloatValues(int fieldIndex);

  Integer getIntegerValue(int fieldIndex);

  List<Integer> getIntegerValues(int fieldIndex);

  Binary getInt96Value(int fieldIndex);

  List<Binary> getInt96Values(int fieldIndex);

  Binary getNanoTimeValue(int fieldIndex);

  List<Binary> getNanoTimeValues(int fieldIndex);

  Long getLongValue(int fieldIndex);

  List<Long> getLongValues(int fieldIndex);

  String getStringValue(int fieldIndex);

  List<String> getStringValues(int fieldIndex);

  Geometry getGeometryValue(int fieldIndex);

  List<Geometry> getGeometryValues(int fieldIndex);

  Envelope getEnvelopeValue(int fieldIndex);

  List<Envelope> getEnvelopeValues(int fieldIndex);

  GeoParquetGroup getGroupValue(int fieldIndex);

  List<GeoParquetGroup> getGroupValues(int fieldIndex);

  Binary getBinaryValue(String fieldName);

  List<Binary> getBinaryValues(String fieldName);

  Boolean getBooleanValue(String fieldName);

  List<Boolean> getBooleanValues(String fieldName);

  Double getDoubleValue(String fieldName);

  List<Double> getDoubleValues(String fieldName);

  Float getFloatValue(String fieldName);

  List<Float> getFloatValues(String fieldName);

  Integer getIntegerValue(String fieldName);

  List<Integer> getIntegerValues(String fieldName);

  Binary getInt96Value(String fieldName);

  List<Binary> getInt96Values(String fieldName);

  Binary getNanoTimeValue(String fieldName);

  List<Binary> getNanoTimeValues(String fieldName);

  Long getLongValue(String fieldName);

  List<Long> getLongValues(String fieldName);

  String getStringValue(String fieldName);

  List<String> getStringValues(String fieldName);

  Geometry getGeometryValue(String fieldName);

  List<Geometry> getGeometryValues(String fieldName);

  Envelope getEnvelopeValue(String fieldName);

  List<Envelope> getEnvelopeValues(String fieldName);

  GeoParquetGroup getGroupValue(String fieldName);

  List<GeoParquetGroup> getGroupValues(String fieldName);

  void setBinaryValue(int fieldIndex, Binary binaryValue);

  void setBinaryValues(int fieldIndex, List<Binary> binaryValues);

  void setBooleanValue(int fieldIndex, Boolean booleanValue);

  void setBooleanValues(int fieldIndex, List<Boolean> booleanValues);

  void setDoubleValue(int fieldIndex, Double doubleValue);

  void setDoubleValues(int fieldIndex, List<Double> doubleValues);

  void setFloatValue(int fieldIndex, Float floatValue);

  void setFloatValues(int fieldIndex, List<Float> floatValues);

  void setIntegerValue(int fieldIndex, Integer integerValue);

  void setIntegerValues(int fieldIndex, List<Integer> integerValues);

  void setInt96Value(int fieldIndex, Binary int96Value);

  void setInt96Values(int fieldIndex, List<Binary> int96Values);

  void setNanoTimeValue(int fieldIndex, Binary nanoTimeValue);

  void setNanoTimeValues(int fieldIndex, List<Binary> nanoTimeValues);

  void setLongValue(int fieldIndex, Long longValue);

  void setLongValues(int fieldIndex, List<Long> longValues);

  void setStringValue(int fieldIndex, String stringValue);

  void setStringValues(int fieldIndex, List<String> stringValues);

  void setGeometryValue(int fieldIndex, Geometry geometryValue);

  void setGeometryValues(int fieldIndex, List<Geometry> geometryValues);

  void setEnvelopeValue(int fieldIndex, Envelope envelopeValue);

  void setEnvelopeValues(int fieldIndex, List<Envelope> envelopeValues);

  void setGroupValue(int fieldIndex, GeoParquetGroup groupValue);

  void setGroupValues(int fieldIndex, List<GeoParquetGroup> groupValues);

  void setBinaryValue(String fieldName, Binary binaryValue);

  void setBinaryValues(String fieldName, List<Binary> binaryValues);

  void setBooleanValue(String fieldName, Boolean booleanValue);

  void setBooleanValues(String fieldName, List<Boolean> booleanValues);

  void setDoubleValue(String fieldName, Double doubleValue);

  void setDoubleValues(String fieldName, List<Double> doubleValues);

  void setFloatValue(String fieldName, Float floatValue);

  void setFloatValues(String fieldName, List<Float> floatValues);

  void setIntegerValue(String fieldName, Integer integerValue);

  void setIntegerValues(String fieldName, List<Integer> integerValues);

  void setInt96Value(String fieldName, Binary int96Value);

  void setInt96Values(String fieldName, List<Binary> int96Values);

  void setNanoTimeValue(String fieldName, Binary nanoTimeValue);

  void setNanoTimeValues(String fieldName, List<Binary> nanoTimeValues);

  void setLongValue(String fieldName, Long longValue);

  void setLongValues(String fieldName, List<Long> longValues);

  void setStringValue(String fieldName, String stringValue);

  void setStringValues(String fieldName, List<String> stringValues);

  void setGeometryValue(String fieldName, Geometry geometryValue);

  void setGeometryValues(String fieldName, List<Geometry> geometryValues);

  void setEnvelopeValue(String fieldName, Envelope envelopeValue);

  void setEnvelopeValues(String fieldName, List<Envelope> envelopeValues);

  void setGroupValue(String fieldName, GeoParquetGroup groupValue);

  void setGroupValues(String fieldName, List<GeoParquetGroup> groupValues);

  /**
   * A GeoParquet schema that describes the fields of a group and can easily be introspected.
   *
   * @param name
   * @param fields the fields of the schema
   */
  record Schema(String name, List<Field> fields) {

  }

  /**
   * A sealed inteface for the fields of a GeoParquet schema.
   * <p>
   * Sealed interfaces were introduced in Java 17 and can be used with pattern matching since Java
   * 21.
   */
  sealed
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

  record GroupField(String name, Cardinality cardinality, Schema schema) implements Field {

    @Override
    public Type type() {
      return Type.GROUP;
    }
  }

  /**
   * The type of a GeoParquet field.
   */
  enum Type {
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
  enum Cardinality {
    REQUIRED,
    OPTIONAL,
    REPEATED
  }

}
