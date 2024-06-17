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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.apache.baremaps.flatgeobuf.generated.Feature;
import org.apache.baremaps.flatgeobuf.generated.Header;

public class FlatGeoBufReader {

  public static Header readHeader(ReadableByteChannel channel)
      throws IOException {

    // Check if the file is a flatgeobuf
    ByteBuffer buffer = BufferUtil.createByteBuffer(12, ByteOrder.LITTLE_ENDIAN);
    BufferUtil.readBytes(channel, buffer, 12);
    if (!Constants.isFlatgeobuf(buffer)) {
      throw new IOException("This is not a flatgeobuf!");
    }

    // Read the header size
    int headerSize = buffer.getInt();
    ByteBuffer headerBuffer = BufferUtil.createByteBuffer(headerSize, ByteOrder.LITTLE_ENDIAN);
    BufferUtil.readBytes(channel, headerBuffer, headerSize);

    return Header.getRootAsHeader(headerBuffer);
  }

  public static void skipIndex(ReadableByteChannel channel, Header header)
      throws IOException {
    readIndexAsBuffer(channel, header);
  }

  public static ByteBuffer readIndexAsBuffer(ReadableByteChannel channel, Header header)
      throws IOException {
    long indexSize = PackedRTree.calcSize(header.featuresCount(), header.indexNodeSize());

    if (indexSize > 1L << 31) {
      throw new IOException("Index size is greater than 2GB!");
    }
    ByteBuffer buffer = BufferUtil.createByteBuffer((int) indexSize, ByteOrder.LITTLE_ENDIAN);
    BufferUtil.readBytes(channel, buffer, (int) indexSize);

    return buffer;
  }

  public static InputStream readIndexAsStream(ReadableByteChannel channel, Header header) {
    long indexSize = PackedRTree.calcSize(header.featuresCount(), header.indexNodeSize());
    return new BoundedInputStream(Channels.newInputStream(channel), indexSize);
  }

  public static Feature readFeature(ReadableByteChannel channel, ByteBuffer buffer)
      throws IOException {
    ByteBuffer newBuffer = BufferUtil.readBytes(channel, buffer, 4);
    int featureSize = newBuffer.getInt();
    newBuffer = BufferUtil.readBytes(channel, buffer, featureSize);
    Feature feature = Feature.getRootAsFeature(newBuffer);
    buffer.position(buffer.position() + featureSize);
    return feature;
  }

  // var geometryBuffer = feature.geometry();
  // var geometry = GeometryConversions.readGeometry(geometryBuffer, geometryBuffer.type());
  // var properties = new ArrayList<>();
  // if (feature.propertiesLength() > 0) {
  // var propertiesBuffer = feature.propertiesAsByteBuffer();
  // while (propertiesBuffer.hasRemaining()) {
  // var type = propertiesBuffer.getShort();
  // var column = header.columns.get(type);
  // var value = readColumnValue(propertiesBuffer, column);
  // properties.add(value);
  // }
  // }
  // }
  //
  // public static Object readColumnValue(ByteBuffer buffer, ColumnMeta column) {
  // return switch (column.type()) {
  // case ColumnType.Byte -> buffer.get();
  // case ColumnType.Bool -> buffer.get() == 1;
  // case ColumnType.Short -> buffer.getShort();
  // case ColumnType.Int -> buffer.getInt();
  // case ColumnType.Long -> buffer.getLong();
  // case ColumnType.Float -> buffer.getFloat();
  // case ColumnType.Double -> buffer.getDouble();
  // case ColumnType.String -> readColumnString(buffer);
  // case ColumnType.Json -> readColumnJson(buffer);
  // case ColumnType.DateTime -> readColumnDateTime(buffer);
  // case ColumnType.Binary -> readColumnBinary(buffer);
  // default -> null;
  // };
  // }
  //
  // public static Object readColumnString(ByteBuffer buffer) {
  // var length = buffer.getInt();
  // var bytes = new byte[length];
  // buffer.get(bytes);
  // return new String(bytes, StandardCharsets.UTF_8);
  // }
  //
  // public static Object readColumnJson(ByteBuffer buffer) {
  // throw new UnsupportedOperationException();
  // }
  //
  // public static Object readColumnDateTime(ByteBuffer buffer) {
  // throw new UnsupportedOperationException();
  // }
  //
  // public static Object readColumnBinary(ByteBuffer buffer) {
  // throw new UnsupportedOperationException();
  // }
}
