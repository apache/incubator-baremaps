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

package org.apache.baremaps.collection;



import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.baremaps.collection.memory.Memory;
import org.apache.baremaps.collection.type.DataType;

/**
 * A data store backed by a {@link DataType} and a {@link Memory}. Data is appended to the store and
 * can be accessed by its position in the {@link Memory}.
 */
public class DataStore<T> implements Closeable, Cleanable {

  private final DataType<T> dataType;
  private final Memory memory;
  private final long segmentSize;
  private long offset;
  private long size;

  private Lock lock = new ReentrantLock();

  /**
   * Constructs a data store.
   *
   * @param dataType the data type
   * @param memory the memory
   */
  public DataStore(DataType<T> dataType, Memory memory) {
    this.dataType = dataType;
    this.memory = memory;
    this.segmentSize = memory.segmentSize();
    this.offset = 0;
    this.size = 0;
  }

  /**
   * Appends a value to the data store and returns its position in the memory.
   *
   * @param value the value
   * @return the position of the value in the memory.
   */
  public long add(T value) {
    int valueSize = dataType.size(value);
    if (valueSize > segmentSize) {
      throw new StoreException("The value is too big to fit in a segment");
    }

    lock.lock();
    long position = offset;
    long segmentIndex = position / segmentSize;
    long segmentOffset = position % segmentSize;
    if (segmentOffset + valueSize > segmentSize) {
      segmentOffset = 0;
      segmentIndex = segmentIndex + 1;
      position = segmentIndex * segmentSize;
    }
    offset = position + valueSize;
    this.size++;
    lock.unlock();

    ByteBuffer segment = memory.segment((int) segmentIndex);
    dataType.write(segment, (int) segmentOffset, value);

    return position;
  }

  /**
   * Returns a values by its position in memory.
   *
   * @param position the position of the value
   * @return the value
   */
  public T get(long position) {
    long segmentIndex = position / segmentSize;
    long segmentOffset = position % segmentSize;
    ByteBuffer buffer = memory.segment((int) segmentIndex);
    return dataType.read(buffer, (int) segmentOffset);
  }

  /**
   * Returns the number of values stored in the data store.
   *
   * @return the number of values
   */
  public long size() {
    return size;
  }

  /** {@inheritDoc} */
  @Override
  public void clean() throws IOException {
    memory.clean();
  }

  /** {@inheritDoc} */
  @Override
  public void close() throws IOException {
    memory.close();
  }
}
