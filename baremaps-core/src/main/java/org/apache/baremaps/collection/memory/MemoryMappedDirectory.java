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
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.baremaps.utils.FileUtils;
import org.apache.baremaps.utils.MappedByteBufferUtils;

/** A memory that stores segments on-disk using mapped byte buffers in a directory of files. */
public class MemoryMappedDirectory extends Memory<MappedByteBuffer> {

  private final Path directory;

  /**
   * Constructs an {@link MemoryMappedDirectory} with a custom directory and a default segment size
   * of 1gb.
   *
   * @param directory the directory that stores the data
   */
  public MemoryMappedDirectory(Path directory) {
    this(directory, 1 << 30);
  }

  /**
   * Constructs an {@link MemoryMappedDirectory} with a custom directory and a custom segment size.
   *
   * @param directory the directory that stores the data
   * @param segmentBytes the size of the segments in bytes
   */
  public MemoryMappedDirectory(Path directory, int segmentBytes) {
    super(segmentBytes);
    this.directory = directory;
  }

  /** {@inheritDoc} */
  @Override
  protected MappedByteBuffer allocate(int index, int size) {
    try {
      Path file = directory.resolve(String.format("%s.part", index));
      try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE,
          StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        return channel.map(MapMode.READ_WRITE, 0, size);
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
    FileUtils.deleteRecursively(directory);
  }

}
