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

import com.google.flatbuffers.FlatBufferBuilder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.IntStream;
import org.apache.baremaps.flatgeobuf.generated.Column;
import org.apache.baremaps.flatgeobuf.generated.Feature;
import org.apache.baremaps.flatgeobuf.generated.Header;

public class FlatGeoBufMapper {

  private FlatGeoBufMapper() {
    // Prevent instantiation
  }

  public static Header asHeaderFlatGeoBuf(FlatGeoBuf.Header header) {
    var builder = new FlatBufferBuilder();
    int[] columnsArray = header.columns().stream().mapToInt(c -> {
      int nameOffset = builder.createString(c.name());
      int type = c.type().ordinal();
      return org.apache.baremaps.flatgeobuf.generated.Column.createColumn(
          builder, nameOffset, type, 0, 0, c.width(), c.precision(), c.scale(), c.nullable(),
          c.unique(),
          c.primaryKey(), 0);
    }).toArray();
    int columnsOffset =
        org.apache.baremaps.flatgeobuf.generated.Header.createColumnsVector(builder, columnsArray);

    int nameOffset = 0;
    if (header.name() != null) {
      nameOffset = builder.createString(header.name());
    }
    int crsOffset = 0;
    if (header.crs().code() != 0) {
      org.apache.baremaps.flatgeobuf.generated.Crs.startCrs(builder);
      org.apache.baremaps.flatgeobuf.generated.Crs.addCode(builder, header.crs().code());
      crsOffset = org.apache.baremaps.flatgeobuf.generated.Crs.endCrs(builder);
    }
    int envelopeOffset = 0;
    if (header.envelope() != null) {
      envelopeOffset = Header.createEnvelopeVector(builder, header.envelope());
    }
    Header.startHeader(builder);
    Header.addGeometryType(builder, header.geometryType().getValue());
    Header.addIndexNodeSize(builder, header.indexNodeSize());
    Header.addColumns(builder, columnsOffset);
    Header.addEnvelope(builder, envelopeOffset);
    Header.addName(builder, nameOffset);
    Header.addCrs(builder, crsOffset);
    Header.addFeaturesCount(builder, header.featuresCount());
    int offset = Header.endHeader(builder);

    builder.finishSizePrefixed(offset);

    return Header.getRootAsHeader(builder.dataBuffer());
  }

  public static FlatGeoBuf.Header asHeaderRecord(Header header) {
    return new FlatGeoBuf.Header(
        header.name(),
        new double[] {
            header.envelope(0),
            header.envelope(1),
            header.envelope(2),
            header.envelope(3)
        },
        FlatGeoBuf.GeometryType.values()[header.geometryType()],
        header.hasZ(),
        header.hasM(),
        header.hasT(),
        header.hasTm(),
        IntStream.range(0, header.columnsLength())
            .mapToObj(header::columns)
            .map(column -> new FlatGeoBuf.Column(
                column.name(),
                FlatGeoBuf.ColumnType.values()[column.type()],
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
        new FlatGeoBuf.Crs(
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

  public static FlatGeoBuf.Feature asFeatureRecord(Header header, Feature feature) {
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
    return new FlatGeoBuf.Feature(
        GeometryConversions.readGeometry(feature.geometry(), header.geometryType()),
        values);
  }

  private static Object readValue(ByteBuffer buffer,
      Column column) {
    return switch (FlatGeoBuf.ColumnType.values()[column.type()]) {
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
