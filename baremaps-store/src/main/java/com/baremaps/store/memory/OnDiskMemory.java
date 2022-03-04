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

import com.baremaps.store.StoreException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** A memory that stores segments on-disk using mapped byte buffers. */
public class OnDiskMemory extends Memory {

  private final Path directory;

  public OnDiskMemory(Path directory) {
    this(directory, 1 << 30);
  }

  public OnDiskMemory(Path directory, int segmentBytes) {
    super(segmentBytes);
    this.directory = directory;
  }

  @Override
  protected ByteBuffer allocate(int index, int size) {
    try {
      Path file = directory.resolve(String.format("%s.part", index));
      try (FileChannel channel =
          FileChannel.open(
              file, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        return channel.map(MapMode.READ_WRITE, 0, size);
      }
    } catch (IOException e) {
      throw new StoreException(e);
    }
  }
}
