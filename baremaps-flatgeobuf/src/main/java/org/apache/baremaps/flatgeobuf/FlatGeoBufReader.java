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
import java.nio.BufferUnderflowException;
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
    if (!FlatGeoBuf.isFlatgeobuf(buffer)) {
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
    readIndexBuffer(channel, header);
  }

  public static ByteBuffer readIndexBuffer(ReadableByteChannel channel, Header header)
      throws IOException {
    long indexSize = PackedRTree.calcSize(header.featuresCount(), header.indexNodeSize());
    if (indexSize > 1L << 31) {
      throw new IOException("Index size is greater than 2GB!");
    }
    ByteBuffer buffer = BufferUtil.createByteBuffer((int) indexSize, ByteOrder.LITTLE_ENDIAN);
    BufferUtil.readBytes(channel, buffer, (int) indexSize);
    return buffer;
  }

  public static InputStream readIndexStream(ReadableByteChannel channel, Header header) {
    long indexSize = PackedRTree.calcSize(header.featuresCount(), header.indexNodeSize());
    return new BoundedInputStream(Channels.newInputStream(channel), indexSize);
  }

  public static Feature readFeature(ReadableByteChannel channel, ByteBuffer buffer)
      throws IOException {
    try {
      ByteBuffer newBuffer = BufferUtil.readBytes(channel, buffer, 1<<16);
      int featureSize = newBuffer.getInt();
      newBuffer = BufferUtil.readBytes(channel, newBuffer, featureSize);
      Feature feature = Feature.getRootAsFeature(newBuffer);
      buffer.position(buffer.position() + featureSize);
      return feature;
    } catch (IOException | BufferUnderflowException e) {
      throw new IOException("Error reading feature", e);
    }
  }

  private static class BoundedInputStream extends InputStream {
    private final InputStream in;
    private long remaining;

    private BoundedInputStream(InputStream in, long size) {
      this.in = in;
      this.remaining = size;
    }

    @Override
    public int read() throws IOException {
      if (remaining == 0) {
        return -1;
      }
      int result = in.read();
      if (result != -1) {
        remaining--;
      }
      return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (remaining == 0) {
        return -1;
      }
      int toRead = (int) Math.min(len, remaining);
      int result = in.read(b, off, toRead);
      if (result != -1) {
        remaining -= result;
      }
      return result;
    }

    @Override
    public long skip(long n) throws IOException {
      long toSkip = Math.min(n, remaining);
      long skipped = in.skip(toSkip);
      remaining -= skipped;
      return skipped;
    }

    @Override
    public int available() throws IOException {
      return (int) Math.min(in.available(), remaining);
    }

    @Override
    public void close() throws IOException {
      in.close();
    }
  }
}
