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

package org.apache.baremaps.collection.type;



import java.nio.ByteBuffer;

/**
 * A data type for reading and writing values in {@link ByteBuffer}s. Read and write operations must
 * use absolute positions within the {@link ByteBuffer}.
 *
 * @param <T>
 */
public interface DataType<T> {

  /**
   * Returns the size of the value in memory.
   *
   * @param value the value
   * @return the size of the value in memory.
   */
  int size(T value);

  /**
   * Write a value.
   *
   * @param buffer the source buffer
   * @param position the absolute position of the value within the buffer
   * @param value the value
   */
  void write(ByteBuffer buffer, int position, T value);

  /**
   * Read a value.
   *
   * @param buffer the source buffer
   * @param position the absolute position of the value within the buffer
   * @return the object
   */
  T read(ByteBuffer buffer, int position);
}
