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

  Long getLongValue(int fieldIndex);

  List<Long> getLongValues(int fieldIndex);

  String getStringValue(int fieldIndex);

  List<String> getStringValues(int fieldIndex);

  Geometry getGeometryValue(int fieldIndex);

  List<Geometry> getGeometryValues(int fieldIndex);

  GeoParquetGroup getGroupValue(int fieldIndex);

  List<GeoParquetGroup> getGroupValues(int fieldIndex);

  Binary getBinaryValue(String columnName);

  List<Binary> getBinaryValues(String columnName);

  Boolean getBooleanValue(String columnName);

  List<Boolean> getBooleanValues(String columnName);

  Double getDoubleValue(String columnName);

  List<Double> getDoubleValues(String columnName);

  Float getFloatValue(String columnName);

  List<Float> getFloatValues(String columnName);

  Integer getIntegerValue(String columnName);

  List<Integer> getIntegerValues(String columnName);

  Long getLongValue(String columnName);

  List<Long> getLongValues(String columnName);

  String getStringValue(String columnName);

  List<String> getStringValues(String columnName);

  Geometry getGeometryValue(String columnName);

  List<Geometry> getGeometryValues(String columnName);

  GeoParquetGroup getGroupValue(String columnName);

  List<GeoParquetGroup> getGroupValues(String columnName);

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

  void setLongValue(int fieldIndex, Long longValue);

  void setLongValues(int fieldIndex, List<Long> longValues);

  void setStringValue(int fieldIndex, String stringValue);

  void setStringValues(int fieldIndex, List<String> stringValues);

  void setGeometryValue(int fieldIndex, Geometry geometryValue);

  void setGeometryValues(int fieldIndex, List<Geometry> geometryValues);

  void setGroupValue(int fieldIndex, GeoParquetGroup groupValue);

  void setGroupValues(int fieldIndex, List<GeoParquetGroup> groupValues);

  void setBinaryValue(String columnName, Binary binaryValue);

  void setBinaryValues(String columnName, List<Binary> binaryValues);

  void setBooleanValue(String columnName, Boolean booleanValue);

  void setBooleanValues(String columnName, List<Boolean> booleanValues);

  void setDoubleValue(String columnName, Double doubleValue);

  void setDoubleValues(String columnName, List<Double> doubleValues);

  void setFloatValue(String columnName, Float floatValue);

  void setFloatValues(String columnName, List<Float> floatValues);

  void setIntegerValue(String columnName, Integer integerValue);

  void setIntegerValues(String columnName, List<Integer> integerValues);

  void setLongValue(String columnName, Long longValue);

  void setLongValues(String columnName, List<Long> longValues);

  void setStringValue(String columnName, String stringValue);

  void setStringValues(String columnName, List<String> stringValues);

  void setGeometryValue(String columnName, Geometry geometryValue);

  void setGeometryValues(String columnName, List<Geometry> geometryValues);

  void setGroupValue(String columnName, GeoParquetGroup groupValue);

  void setGroupValues(String columnName, List<GeoParquetGroup> groupValues);

  /**
   * A GeoParquet schema that describes the fields of a group and can easily be introspected.
   *
   * @param fields the fields of the schema
   */
  record Schema(List<Field> fields) {

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
    LONG,
    STRING,
    GEOMETRY,
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
