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
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class OnDiskMemory extends Memory {

  private final Path directory;

  public OnDiskMemory() throws IOException {
    this(Files.createTempDirectory("tmp_"), 1 << 20);
  }

  public OnDiskMemory(int segmentBytes) throws IOException {
    this(Files.createTempDirectory("tmp_"), segmentBytes);
  }

  public OnDiskMemory(Path directory, int segmentBytes) throws IOException {
    super(segmentBytes);
    this.directory = directory;
  }

  @Override
  protected ByteBuffer allocateSegment(int index, int size) {
    try {
      Path file = directory.resolve(String.format("%s.part", index));
      try (FileChannel channel =
          FileChannel.open(
              file,
              StandardOpenOption.CREATE,
              StandardOpenOption.READ,
              StandardOpenOption.WRITE)) {
        return channel.map(MapMode.READ_WRITE, 0, size);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
