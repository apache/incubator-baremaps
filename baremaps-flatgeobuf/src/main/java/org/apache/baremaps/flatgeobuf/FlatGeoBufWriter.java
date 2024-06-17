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

//
// import com.google.flatbuffers.FlatBufferBuilder;
// import java.io.IOException;
// import java.io.OutputStream;
// import java.nio.ByteBuffer;
// import java.nio.ByteOrder;
// import java.nio.channels.Channels;
// import java.nio.channels.WritableByteChannel;
// import java.nio.charset.StandardCharsets;
//
// import org.wololo.flatgeobuf.generated.*;
//
// public class FlatGeoBufWriter {
// public static void writeColumnValue(ByteBuffer buffer, ColumnMeta column, Object value) {
// switch (column.type()) {
// case ColumnType.Byte -> buffer.put((byte) value);
// case ColumnType.Bool -> buffer.put((byte) ((boolean) value ? 1 : 0));
// case ColumnType.Short -> buffer.putShort((short) value);
// case ColumnType.Int -> buffer.putInt((int) value);
// case ColumnType.Long -> buffer.putLong((long) value);
// case ColumnType.Float -> buffer.putFloat((float) value);
// case ColumnType.Double -> buffer.putDouble((double) value);
// case ColumnType.String -> writeColumnString(buffer, value);
// case ColumnType.Json -> writeColumnJson(buffer, value);
// case ColumnType.DateTime -> writeColumnDateTime(buffer, value);
// case ColumnType.Binary -> writeColumnBinary(buffer, value);
// default -> {
// // Do nothing
// }
// };
// }
//
// public static void writeColumnString(ByteBuffer propertiesBuffer, Object value) {
// var bytes = ((String) value).getBytes(StandardCharsets.UTF_8);
// propertiesBuffer.putInt(bytes.length);
// propertiesBuffer.put(bytes);
// }
//
// public static void writeColumnJson(ByteBuffer propertiesBuffer, Object value) {
// throw new UnsupportedOperationException();
// }
//
// public static void writeColumnDateTime(ByteBuffer propertiesBuffer, Object value) {
// throw new UnsupportedOperationException();
// }
//
// public static void writeColumnBinary(ByteBuffer propertiesBuffer, Object value) {
// throw new UnsupportedOperationException();
// }
//
// public static void writeFeature(
// OutputStream outputStream, HeaderMeta headerMeta,
// FeatureMeta featureMeta) throws IOException {
// var featureBuilder = new FlatBufferBuilder(4096);
//
// // Write the properties
// var propertiesBuffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
// var properties = featureMeta.properties();
// for (int i = 0; i < properties.size(); i++) {
// var column = headerMeta.columns.get(i);
// var value = properties.get(i);
// propertiesBuffer.putShort((short) i);
// writeColumnValue(propertiesBuffer, column, value);
// }
// if (propertiesBuffer.position() > 0) {
// propertiesBuffer.flip();
// }
// var propertiesOffset = Feature.createPropertiesVector(featureBuilder, propertiesBuffer);
//
// // Write the geometry
// var geometry = featureMeta.geometry();
// var geometryOffset = 0;
// if (geometry != null) {
// geometryOffset =
// GeometryConversions.writeGeometry(featureBuilder, geometry, headerMeta.geometryType);
// }
//
// // Write the feature
// var featureOffset = Feature.createFeature(featureBuilder, geometryOffset, propertiesOffset, 0);
// featureBuilder.finishSizePrefixed(featureOffset);
//
// byte[] data = featureBuilder.sizedByteArray();
// outputStream.write(data);
// }
//
// public static void write(HeaderMeta headerMeta, OutputStream to, FlatBufferBuilder builder)
// throws IOException {
// int[] columnsArray = headerMeta.columns.stream().mapToInt(c -> {
// int nameOffset = builder.createString(c.name());
// int type = c.type();
// return Column.createColumn(
// builder,
// nameOffset,
// type,
// 0,
// 0,
// c.width(),
// c.precision(),
// c.scale(),
// c.nullable(),
// c.unique(),
// c.primaryKey(),
// 0);
// }).toArray();
// int columnsOffset = Header.createColumnsVector(builder, columnsArray);
//
// int nameOffset = 0;
// if (headerMeta.name != null) {
// nameOffset = builder.createString(headerMeta.name);
// }
// int crsOffset = 0;
// if (headerMeta.srid != 0) {
// Crs.startCrs(builder);
// Crs.addCode(builder, headerMeta.srid);
// crsOffset = Crs.endCrs(builder);
// }
// int envelopeOffset = 0;
// if (headerMeta.envelope != null) {
// envelopeOffset = Header.createEnvelopeVector(builder,
// new double[] {headerMeta.envelope.getMinX(), headerMeta.envelope.getMinY(),
// headerMeta.envelope.getMaxX(), headerMeta.envelope.getMaxY()});
// }
// Header.startHeader(builder);
// Header.addGeometryType(builder, headerMeta.geometryType);
// Header.addIndexNodeSize(builder, headerMeta.indexNodeSize);
// Header.addColumns(builder, columnsOffset);
// Header.addEnvelope(builder, envelopeOffset);
// Header.addName(builder, nameOffset);
// Header.addCrs(builder, crsOffset);
// Header.addFeaturesCount(builder, headerMeta.featuresCount);
// int offset = Header.endHeader(builder);
//
// builder.finishSizePrefixed(offset);
//
// WritableByteChannel channel = Channels.newChannel(to);
// ByteBuffer dataBuffer = builder.dataBuffer();
// while (dataBuffer.hasRemaining()) {
// channel.write(dataBuffer);
// }
// }
// }
