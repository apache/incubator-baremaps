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


import static org.apache.baremaps.flatgeobuf.FlatGeoBufReader.readValue;

import com.google.flatbuffers.FlatBufferBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.baremaps.flatgeobuf.generated.*;
import org.locationtech.jts.geom.Geometry;

public class FlatGeoBufWriter {

  public static void writeHeaderRecord(WritableByteChannel channel, FlatGeoBuf.Header header)
      throws IOException {
    Header headerFlatGeoBuf = asHeaderRecord(header);
    writeHeaderFlatGeoBuf(channel, headerFlatGeoBuf);
  }

  public static void writeHeaderFlatGeoBuf(WritableByteChannel channel, Header header) throws IOException {
    ByteBuffer headerBuffer = header.getByteBuffer();
    ByteBuffer startBuffer = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
    startBuffer.put(FlatGeoBuf.MAGIC_BYTES);
    startBuffer.putInt(headerBuffer.remaining());
    startBuffer.flip();
    while (startBuffer.hasRemaining()) {
      channel.write(startBuffer);
    }
    while (headerBuffer.hasRemaining()) {
      channel.write(headerBuffer);
    }
  }

  public static void writeIndexStream(WritableByteChannel channel, InputStream inputStream)
      throws IOException {
    try (OutputStream outputStream = Channels.newOutputStream(channel)) {
      outputStream.write(inputStream.readAllBytes());
    }
  }

  public static void writeIndexBuffer(WritableByteChannel channel, ByteBuffer buffer)
      throws IOException {
    while (buffer.hasRemaining()) {
      channel.write(buffer);
    }
  }

  public static void writeFeatureFlatGeoBuf(WritableByteChannel channel, Feature feature) throws IOException {
    ByteBuffer sizeBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
    sizeBuffer.putInt(feature.getByteBuffer().remaining());
    sizeBuffer.flip();
    while (sizeBuffer.hasRemaining()) {
      channel.write(sizeBuffer);
    }
    ByteBuffer featureBuffer = feature.getByteBuffer().duplicate();
    while (featureBuffer.hasRemaining()) {
      channel.write(featureBuffer);
    }
  }

  public static void writeFeatureRecord(
      WritableByteChannel channel,
      Header header,
      FlatGeoBuf.Feature feature) throws IOException {
    Feature featureRecord = writeFeature(header, feature);
    writeFeatureFlatGeoBuf(channel, featureRecord);
  }

  public static Header asHeaderRecord(FlatGeoBuf.Header header) {
    FlatBufferBuilder builder = new FlatBufferBuilder(4096);

    int[] columnsArray = header.columns().stream().mapToInt(c -> {
      int nameOffset = builder.createString(c.name());
      int type = c.type().ordinal();
      return Column.createColumn(
          builder, nameOffset, type, 0, 0, c.width(), c.precision(), c.scale(), c.nullable(),
          c.unique(),
          c.primaryKey(), 0);
    }).toArray();
    int columnsOffset =
        Header.createColumnsVector(builder, columnsArray);

    int envelopeOffset = 0;
    if (header.envelope() != null) {
      envelopeOffset = Header.createEnvelopeVector(builder,
          header.envelope().stream().mapToDouble(d -> d).toArray());
    }

    int nameOffset = 0;
    if (header.name() != null) {
      nameOffset = builder.createString(header.name());
    }

    int crsOrgOffset = 0;
    if (header.crs().org() != null) {
      crsOrgOffset = builder.createString(header.crs().org());
    }

    int crsNameOffset = 0;
    if (header.crs().name() != null) {
      crsNameOffset = builder.createString(header.crs().name());
    }

    int crsDescriptionOffset = 0;
    if (header.crs().description() != null) {
      crsDescriptionOffset = builder.createString(header.crs().description());
    }

    int crsWktOffset = 0;
    if (header.crs().wkt() != null) {
      crsWktOffset = builder.createString(header.crs().wkt());
    }

    int crsCodeStringOffset = 0;
    if (header.crs().codeString() != null) {
      crsCodeStringOffset = builder.createString(header.crs().codeString());
    }

    Crs.startCrs(builder);
    Crs.addOrg(builder, crsOrgOffset);
    Crs.addCode(builder, header.crs().code());
    Crs.addName(builder, crsNameOffset);
    Crs.addDescription(builder, crsDescriptionOffset);
    Crs.addWkt(builder, crsWktOffset);
    Crs.addCodeString(builder, crsCodeStringOffset);
    int crsOffset = Crs.endCrs(builder);



    Header.startHeader(builder);
    Header.addGeometryType(builder, header.geometryType().getValue());
    Header.addFeaturesCount(builder, header.featuresCount());
    Header.addIndexNodeSize(builder, header.indexNodeSize());
    Header.addColumns(builder, columnsOffset);
    Header.addEnvelope(builder, envelopeOffset);
    Header.addName(builder, nameOffset);
    Header.addCrs(builder, crsOffset);

    int offset = Header.endHeader(builder);
    builder.finish(offset);

    ByteBuffer buffer = builder.dataBuffer().asReadOnlyBuffer();
    return Header.getRootAsHeader(buffer);
  }

  public static void writeValue(ByteBuffer buffer, Column column, Object value) {
    switch (column.type()) {
      case ColumnType.Bool -> buffer.put((byte) ((boolean) value ? 1 : 0));
      case ColumnType.Short -> buffer.putShort((short) value);
      case ColumnType.UShort -> buffer.putShort((short) value);
      case ColumnType.Int -> buffer.putInt((int) value);
      case ColumnType.UInt -> buffer.putInt((int) value);
      case ColumnType.Long -> buffer.putLong((long) value);
      case ColumnType.ULong -> buffer.putLong((long) value);
      case ColumnType.Float -> buffer.putFloat((float) value);
      case ColumnType.Double -> buffer.putDouble((double) value);
      case ColumnType.String -> writeColumnString(buffer, value);
      case ColumnType.Json -> writeColumnJson(buffer, value);
      case ColumnType.DateTime -> writeColumnDateTime(buffer, value);
      case ColumnType.Binary -> writeColumnBinary(buffer, value);
    }
  }

  public static void writeColumnString(ByteBuffer propertiesBuffer, Object value) {
    var bytes = ((String) value).getBytes(StandardCharsets.UTF_8);
    propertiesBuffer.putInt(bytes.length);
    propertiesBuffer.put(bytes);
  }

  public static void writeColumnJson(ByteBuffer propertiesBuffer, Object value) {
    throw new UnsupportedOperationException();
  }

  public static void writeColumnDateTime(ByteBuffer propertiesBuffer, Object value) {
    throw new UnsupportedOperationException();
  }

  public static void writeColumnBinary(ByteBuffer propertiesBuffer, Object value) {
    throw new UnsupportedOperationException();
  }

  public static Feature writeFeature(Header header, FlatGeoBuf.Feature feature)
      throws IOException {
    FlatBufferBuilder builder = new FlatBufferBuilder(4096);

    // Write the properties
    ByteBuffer propertiesBuffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
    List<Object> properties = feature.properties();
    for (int i = 0; i < properties.size(); i++) {
      var column = header.columns(i);
      var value = properties.get(i);
      propertiesBuffer.putShort((short) i);
      writeValue(propertiesBuffer, column, value);
    }
    if (propertiesBuffer.position() > 0) {
      propertiesBuffer.flip();
    }
    int propertiesOffset = Feature.createPropertiesVector(builder, propertiesBuffer);

    // Write the geometry
    Geometry geometry = feature.geometry();
    int geometryOffset = 0;
    if (geometry != null) {
      geometryOffset =
          GeometryConversions.writeGeometry(builder, geometry,
              (byte) header.geometryType());
    }

    // Write the feature
    Feature.startFeature(builder);
    Feature.addGeometry(builder, geometryOffset);
    Feature.addProperties(builder, propertiesOffset);
    Feature.addColumns(builder, 0);

    int offset = Feature.endFeature(builder);
    builder.finish(offset);

    ByteBuffer buffer = builder.dataBuffer().asReadOnlyBuffer();
    return Feature.getRootAsFeature(buffer);
  }

  public static FlatGeoBuf.Feature writeFeatureFlatGeoBuf(Header header, Feature feature) {
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
        values, GeometryConversions.readGeometry(feature.geometry(), header.geometryType()));
  }
}
