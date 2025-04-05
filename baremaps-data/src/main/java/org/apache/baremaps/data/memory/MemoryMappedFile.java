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

package org.apache.baremaps.data.memory;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.baremaps.data.util.MappedByteBufferUtils;

/** Memory implementation that uses memory-mapped file for storage. */
public class MemoryMappedFile extends Memory<MappedByteBuffer> {

  private final Path file;

  /**
   * Constructs a MemoryMappedFile with the specified file and 1GB segment size.
   *
   * @param file the file to memory-map
   */
  public MemoryMappedFile(Path file) {
    this(file, 1 << 30);
  }

  /**
   * Constructs a MemoryMappedFile with the specified file and segment size.
   *
   * @param file the file to memory-map
   * @param segmentBytes the size of each segment in bytes
   */
  public MemoryMappedFile(Path file, int segmentBytes) {
    super(segmentBytes);
    this.file = file;
  }

  /** {@inheritDoc} */
  @Override
  protected MappedByteBuffer allocate(int index, int size) {
    try {
      try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE,
          StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        return channel.map(MapMode.READ_WRITE, (long) index * size, size);
      }
    } catch (IOException e) {
      throw new MemoryException(e);
    }
  }

  /**
   * Releases all mapped buffers but keeps the file intact.
   * 
   * {@inheritDoc}
   */
  @Override
  public synchronized void close() throws IOException {
    // Release MappedByteBuffer resources
    for (MappedByteBuffer buffer : segments) {
      if (buffer != null) {
        MappedByteBufferUtils.unmap(buffer);
      }
    }
  }

  /**
   * Unmaps all buffers and deletes the file.
   * 
   * {@inheritDoc}
   */
  @Override
  public synchronized void clear() throws IOException {
    // Release resources first
    close();

    // Clear the segment list
    segments.clear();

    // Delete the file
    Files.deleteIfExists(file);
  }
}
