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
import java.nio.ByteBuffer;

/** A memory that stores segments off-heap using direct byte buffers. */
public class OffHeapMemory extends Memory<ByteBuffer> {

  /** Constructs an {@link OffHeapMemory} with a default segment size of 1mb. */
  public OffHeapMemory() {
    this(1 << 20);
  }

  /**
   * Constructs an {@link OffHeapMemory} with a custom segment size.
   *
   * @param segmentSize the size of the segments in bytes
   */
  public OffHeapMemory(int segmentSize) {
    super(segmentSize);
  }

  /** {@inheritDoc} */
  @Override
  protected ByteBuffer allocate(int index, int size) {
    return ByteBuffer.allocateDirect(size);
  }

  /** {@inheritDoc} */
  @Override
  public void close() throws IOException {
    // Nothing to close
  }

  /** {@inheritDoc} */
  @Override
  public void clean() throws IOException {
    // Nothing to clean
  }
}
