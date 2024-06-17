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

package org.apache.baremaps.flatgeobuf;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.baremaps.flatgeobuf.FlatGeoBuf.Header.Feature;
import org.locationtech.jts.geom.Geometry;

public class FlatGeoBuf {

  private FlatGeoBuf() {
    // Prevent instantiation
  }

  // Geometry type enumeration
  public enum GeometryType {
    UNKNOWN(0),
    POINT(1),
    LINESTRING(2),
    POLYGON(3),
    MULTIPOINT(4),
    MULTILINESTRING(5),
    MULTIPOLYGON(6),
    GEOMETRYCOLLECTION(7),
    CIRCULARSTRING(8),
    COMPOUNDCURVE(9),
    CURVEPOLYGON(10),
    MULTICURVE(11),
    MULTISURFACE(12),
    CURVE(13),
    SURFACE(14),
    POLYHEDRALSURFACE(15),
    TIN(16),
    TRIANGLE(17);

    private final int value;

    GeometryType(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  public enum ColumnType {
    BYTE,
    UBYTE,
    BOOL,
    SHORT,
    USHORT,
    INT,
    UINT,
    LONG,
    ULONG,
    FLOAT,
    DOUBLE,
    STRING,
    JSON,
    DATETIME,
    BINARY
  }

  public record Column(
      String name,
      ColumnType type,
      String title,
      String description,
      int width,
      int precision,
      int scale,
      boolean nullable,
      boolean unique,
      boolean primaryKey,
      String metadata) {
  }

  public record Crs(
      String org,
      int code,
      String name,
      String description,
      String wkt,
      String codeString) {
  }

  public record Header(
      String name,
      double[] envelope,
      GeometryType geometryType,
      boolean hasZ,
      boolean hasM,
      boolean hasT,
      boolean hasTM,
      List<Column> columns,
      long featuresCount,
      int indexNodeSize,
      Crs crs,
      String title,
      String description,
      String metadata) {
    public Header {
      indexNodeSize = indexNodeSize == 0 ? 16 : indexNodeSize;
    }

    public record Feature(
        Geometry geometry,
        List<Object> properties) {
    }
  }

  public static Header asHeaderRecord(org.apache.baremaps.flatgeobuf.generated.Header header) {
    return new Header(
        header.name(),
        new double[] {
            header.envelope(0),
            header.envelope(1),
            header.envelope(2),
            header.envelope(3)
        },
        GeometryType.values()[header.geometryType()],
        header.hasZ(),
        header.hasM(),
        header.hasT(),
        header.hasTm(),
        IntStream.range(0, header.columnsLength())
            .mapToObj(header::columns)
            .map(column -> new Column(
                column.name(),
                ColumnType.values()[column.type()],
                column.title(),
                column.description(),
                column.width(),
                column.precision(),
                column.scale(),
                column.nullable(),
                column.unique(),
                column.primaryKey(),
                column.metadata()))
            .toList(),
        header.featuresCount(),
        header.indexNodeSize(),
        new Crs(
            header.crs().org(),
            header.crs().code(),
            header.crs().name(),
            header.crs().description(),
            header.crs().wkt(),
            header.crs().codeString()),
        header.title(),
        header.description(),
        header.metadata());
  }

  public static Feature asFeatureRecord(org.apache.baremaps.flatgeobuf.generated.Header header,
      org.apache.baremaps.flatgeobuf.generated.Feature feature) {
    var values = new ArrayList<>();
    if (feature.propertiesLength() > 0) {
      var propertiesBuffer = feature.propertiesAsByteBuffer();
      while (propertiesBuffer.hasRemaining()) {
        var columnPosition = propertiesBuffer.getShort();
        var columnType = header.columns(columnPosition);
        var columnValue = readValue(propertiesBuffer, columnType);
        values.add(columnValue);
      }
    }
    return new Feature(
        GeometryConversions.readGeometry(feature.geometry(), header.geometryType()),
        values);
  }

  private static Object readValue(ByteBuffer buffer,
      org.apache.baremaps.flatgeobuf.generated.Column column) {
    return switch (ColumnType.values()[column.type()]) {
      case BYTE -> buffer.get();
      case UBYTE -> buffer.get();
      case BOOL -> buffer.get() == 1;
      case SHORT -> buffer.getShort();
      case USHORT -> buffer.getShort();
      case INT -> buffer.getInt();
      case UINT -> buffer.getInt();
      case LONG -> buffer.getLong();
      case ULONG -> buffer.getLong();
      case FLOAT -> buffer.getFloat();
      case DOUBLE -> buffer.getDouble();
      case STRING -> readString(buffer);
      case JSON -> readJson(buffer);
      case DATETIME -> readDateTime(buffer);
      case BINARY -> readBinary(buffer);
    };
  }

  private static Object readString(ByteBuffer buffer) {
    var length = buffer.getInt();
    var bytes = new byte[length];
    buffer.get(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private static Object readJson(ByteBuffer buffer) {
    throw new UnsupportedOperationException();
  }

  private static Object readDateTime(ByteBuffer buffer) {
    throw new UnsupportedOperationException();
  }

  private static Object readBinary(ByteBuffer buffer) {
    throw new UnsupportedOperationException();
  }
}
