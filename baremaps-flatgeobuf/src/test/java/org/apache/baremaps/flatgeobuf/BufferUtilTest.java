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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.junit.jupiter.api.Test;

public class BufferUtilTest {

  @Test
  void testPrepareBufferAlreadySufficient() throws IOException {
    byte[] data = "Hello, World!".getBytes();
    ReadableByteChannel channel = Channels.newChannel(new ByteArrayInputStream(data));
    ByteBuffer buffer = ByteBuffer.allocate(15);
    buffer.put(data);
    buffer.flip();

    ByteBuffer result = BufferUtil.readBytes(channel, buffer, 5);
    assertEquals(buffer, result);
    assertEquals(13, result.remaining());
  }

  @Test
  void testPrepareBufferCompactAndRead() throws IOException {
    byte[] data = "Hello, World!".getBytes();
    ReadableByteChannel channel = Channels.newChannel(new ByteArrayInputStream(data));
    ByteBuffer buffer = ByteBuffer.allocate(15);
    buffer.put("Hello".getBytes());
    buffer.flip();

    ByteBuffer result = BufferUtil.readBytes(channel, buffer, 10);
    assertEquals(buffer, result);
    assertTrue(result.remaining() >= 10);
  }

  @Test
  void testPrepareBufferAllocateNewBuffer() throws IOException {
    byte[] data = "Hello, World!".getBytes();
    ReadableByteChannel channel = Channels.newChannel(new ByteArrayInputStream(data));
    ByteBuffer buffer = ByteBuffer.allocate(5);
    buffer.put("Hi".getBytes());
    buffer.flip();

    ByteBuffer result = BufferUtil.readBytes(channel, buffer, 10);
    assertNotEquals(buffer, result);
    assertTrue(result.capacity() >= 10);
    assertTrue(result.remaining() >= 10);
  }

  @Test
  void testPrepareBufferWithExactCapacity() throws IOException {
    byte[] data = "Hello, World!".getBytes();
    ReadableByteChannel channel = Channels.newChannel(new ByteArrayInputStream(data));
    ByteBuffer buffer = ByteBuffer.allocate(13);
    buffer.put(data, 0, 5);
    buffer.flip();

    ByteBuffer result = BufferUtil.readBytes(channel, buffer, 10);
    assertEquals(buffer, result);
    assertTrue(result.remaining() >= 10);
  }

  @Test
  void testPrepareEndOfChannel() throws IOException {
    byte[] data = "Hello".getBytes();
    ReadableByteChannel channel = Channels.newChannel(new ByteArrayInputStream(data));
    ByteBuffer buffer = ByteBuffer.allocate(10);
    buffer.put("Hi".getBytes());
    buffer.flip();

    ByteBuffer result = BufferUtil.readBytes(channel, buffer, 10);
    assertEquals(buffer, result);
    assertTrue(result.remaining() <= 10);
  }

  @Test
  void testPrepareNullChannel() {
    ByteBuffer buffer = ByteBuffer.allocate(10);
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
      BufferUtil.readBytes(null, buffer, 10);
    });
    assertEquals("Channel and buffer must not be null", thrown.getMessage());
  }

  @Test
  void testPrepareNullBuffer() {
    byte[] data = "Hello".getBytes();
    ReadableByteChannel channel = Channels.newChannel(new ByteArrayInputStream(data));
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
      BufferUtil.readBytes(channel, null, 10);
    });
    assertEquals("Channel and buffer must not be null", thrown.getMessage());
  }

  @Test
  void testPrepareNegativeBytes() {
    byte[] data = "Hello".getBytes();
    ReadableByteChannel channel = Channels.newChannel(new ByteArrayInputStream(data));
    ByteBuffer buffer = ByteBuffer.allocate(10);
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
      BufferUtil.readBytes(channel, buffer, -1);
    });
    assertEquals("The number of bytes to read must be non-negative", thrown.getMessage());
  }

}
