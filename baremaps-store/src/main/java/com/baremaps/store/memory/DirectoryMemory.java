/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.store.memory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class DirectoryMemory implements Memory {

  private final int segmentBytes;

  private final int segmentBits;

  private final long segmentMask;

  private final Path directory;

  private final int capacity;

  private final List<MappedByteBuffer> segments = new ArrayList<>();

  public DirectoryMemory() throws IOException {
    this(Files.createTempDirectory("tmp_"), 1 << 30);
  }

  public DirectoryMemory(int segmentBytes) throws IOException {
    this(Files.createTempDirectory("tmp_"), segmentBytes);
  }

  public DirectoryMemory(Path directory, int segmentBytes) throws IOException {
    if ((segmentBytes & -segmentBytes) != segmentBytes) {
      throw new IllegalArgumentException("The segment size must be a power of 2");
    }
    this.segmentBytes = segmentBytes;
    this.segmentBits = (int) (Math.log(segmentBytes) / Math.log(2));
    this.segmentMask = (1L << segmentBits) - 1;
    this.directory = directory;
    this.capacity = segmentBytes;
  }

  public final Path file() {
    return directory;
  }

  @Override
  public int segmentBytes() {
    return segmentBytes;
  }

  @Override
  public long segmentBits() {
    return segmentBytes;
  }

  @Override
  public long segmentMask() {
    return segmentMask;
  }

  @Override
  public ByteBuffer segment(int index) {
    while (segments.size() <= index) {
      segments.add(null);
    }
    ByteBuffer segment = segments.get(index);
    if (segment == null) {
      segment = newSegment(index);
    }
    return segment;
  }

  private synchronized ByteBuffer newSegment(int index) {
    MappedByteBuffer buffer = segments.get(index);
    if (buffer == null) {
      try {
        Path file = directory.resolve(String.format("%s.data", index));
        try (FileChannel channel =
            FileChannel.open(
                file,
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE)) {
          buffer = channel.map(MapMode.READ_WRITE, index * (long) capacity, capacity);
          segments.set(index, buffer);
        }
      } catch (IOException e) {
        throw new RuntimeException();
      }
    }
    return buffer;
  }

  @Override
  public void close() throws Exception {
    for (MappedByteBuffer segment : segments) {
      segment.force();
    }
  }
}
