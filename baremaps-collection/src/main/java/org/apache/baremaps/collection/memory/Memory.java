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



import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.baremaps.collection.Cleanable;

/** A base class to manage segments of on-heap, off-heap, or on-disk memory. */
public abstract class Memory<T extends ByteBuffer> implements Closeable, Cleanable {

  private final int segmentSize;

  private final long segmentShift;

  private final long segmentMask;

  protected final List<T> segments = new ArrayList<>();

  protected Memory(int segmentSize) {
    if ((segmentSize & -segmentSize) != segmentSize) {
      throw new IllegalArgumentException("The segment size must be a power of 2");
    }
    this.segmentSize = segmentSize;
    this.segmentShift = (long) (Math.log(this.segmentSize) / Math.log(2));
    this.segmentMask = this.segmentSize - 1l;
  }

  /**
   * Returns the size of the segments.
   *
   * @return the size of the segments
   */
  public int segmentSize() {
    return segmentSize;
  }

  /**
   * Returns the bit shift to find a segment index from a memory position.
   *
   * @return the bit shift
   */
  public long segmentShift() {
    return segmentShift;
  }

  /**
   * Returns the bit mask to find a segment offset from a memory position.
   *
   * @return the bit mask
   */
  public long segmentMask() {
    return segmentMask;
  }

  /**
   * Returns a segment of the memory.
   *
   * @param index the index of the segment
   * @return the segment
   */
  public ByteBuffer segment(int index) {
    if (segments.size() <= index) {
      return allocate(index);
    }
    ByteBuffer segment = segments.get(index);
    if (segment == null) {
      return allocate(index);
    }
    return segment;
  }

  /** The allocation of segments is synchronized to enable access by multiple threads. */
  private synchronized ByteBuffer allocate(int index) {
    while (segments.size() <= index) {
      segments.add(null);
    }
    T segment = segments.get(index);
    if (segment == null) {
      segment = allocate(index, segmentSize);
      segments.set(index, segment);
    }
    return segment;
  }

  /**
   * Allocates a segment for a given index and size.
   *
   * @param index the index of the segment
   * @param size the size of the segment
   * @return the segment
   */
  protected abstract T allocate(int index, int size);
}
