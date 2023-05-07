/*
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

package org.apache.baremaps.collection.memory;



import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.baremaps.utils.MappedByteBufferUtils;

/** A memory that stores segments on-disk using mapped byte buffers in a file. */
public class MemoryMappedFile extends Memory<MappedByteBuffer> {

  private final Path file;

  /**
   * Constructs an {@link MemoryMappedFile} with a custom file and a default segment size of 1gb.
   *
   * @param file the file that stores the data
   */
  public MemoryMappedFile(Path file) {
    this(file, 1 << 30);
  }

  /**
   * Constructs an {@link MemoryMappedFile} with a custom file and a custom segment size.
   *
   * @param file the file that stores the data
   * @param segmentBytes the size of the segments in bytes
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

  /** {@inheritDoc} */
  @Override
  public void close() throws IOException {
    for (MappedByteBuffer buffer : segments) {
      MappedByteBufferUtils.unmap(buffer);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void clear() throws IOException {
    close();
    segments.clear();
    Files.delete(file);
  }
}
