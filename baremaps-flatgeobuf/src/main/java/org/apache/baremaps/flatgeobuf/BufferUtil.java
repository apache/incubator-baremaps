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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;

public class BufferUtil {

  private BufferUtil() {
    // Prevent instantiation
  }

  public static ByteBuffer createByteBuffer(int capacity, ByteOrder order) {
    ByteBuffer buffer = ByteBuffer.allocate(capacity).order(order);
    buffer.flip();
    return buffer;
  }

  /**
   * Skips the given number of bytes from the specified channel, accounting for the bytes already in
   * the buffer.
   *
   * @param channel the channel to skip bytes from
   * @param buffer the buffer to use
   * @param bytesToSkip the number of bytes to skip
   * @return the buffer after skipping the specified number of bytes
   * @throws IOException if an I/O error occurs while reading from the channel
   */
  public static ByteBuffer skipBytes(ReadableByteChannel channel, ByteBuffer buffer,
      long bytesToSkip) throws IOException {
    if (channel == null || buffer == null) {
      throw new IllegalArgumentException("Channel and buffer must not be null");
    }

    if (bytesToSkip < 0) {
      throw new IllegalArgumentException("The number of bytes to skip must be non-negative");
    }

    // If the buffer already has `bytesToSkip` or more bytes remaining, simply adjust the position.
    if (buffer.remaining() >= bytesToSkip) {
      buffer.position(buffer.position() + (int) bytesToSkip);
      return buffer;
    }

    // Calculate the number of bytes we still need to skip after accounting for the buffer's
    // remaining bytes.
    long remainingBytesToSkip = bytesToSkip - buffer.remaining();

    // Clear the buffer to prepare it for reading.
    buffer.clear();

    // Skip bytes directly from the channel.
    while (remainingBytesToSkip > 0) {
      // Read into the buffer to discard the data.
      int bytesRead = channel.read(buffer);
      if (bytesRead == -1) {
        break; // End of channel reached
      }
      remainingBytesToSkip -= bytesRead;
      buffer.clear();
    }

    return buffer;
  }

  /**
   * Prepares the given buffer for reading at least `n` bytes from the specified channel.
   *
   * @param channel the channel to read bytes from
   * @param buffer the buffer to prepare for reading
   * @param bytesToRead the minimum number of bytes the buffer should contain
   * @return a ByteBuffer that contains at least `n` bytes read from the channel
   * @throws IOException if an I/O error occurs while reading from the channel
   */
  public static ByteBuffer readBytes(ReadableByteChannel channel, ByteBuffer buffer,
      int bytesToRead) throws IOException {
    if (channel == null || buffer == null) {
      throw new IllegalArgumentException("Channel and buffer must not be null");
    }

    if (bytesToRead < 0) {
      throw new IllegalArgumentException("The number of bytes to read must be non-negative");
    }

    // If the buffer already has `n` or more bytes remaining, it will be returned as is.
    if (buffer.remaining() >= bytesToRead) {
      return buffer;
    }

    // If the buffer has sufficient capacity but fewer than `n` bytes remaining, compact it and read
    // more bytes.
    if (buffer.capacity() >= bytesToRead) {
      buffer.compact();
      while (buffer.position() < bytesToRead) {
        if (channel.read(buffer) == -1) {
          break; // End of channel reached
        }
      }
      buffer.flip();
      return buffer;
    }

    // If the buffer has insufficient capacity, allocate a new buffer with the required capacity.
    ByteBuffer newBuffer = ByteBuffer.allocate(bytesToRead).order(buffer.order());
    buffer.flip();
    newBuffer.put(buffer);
    while (newBuffer.position() < bytesToRead) {
      if (channel.read(newBuffer) == -1) {
        break; // End of channel reached
      }
    }
    newBuffer.flip();
    return newBuffer;
  }
}
