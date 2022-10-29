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

/** A {@link DataType} for reading and writing small integers in {@link ByteBuffer}s. */
public class SmallIntegerDataType implements SizedDataType<Integer> {

  private final int n;

  /**
   * Constructs a {@link SmallIntegerDataType}.
   *
   * @param n the number of bytes used to store the integer
   */
  public SmallIntegerDataType(int n) {
    if (n < 1 || n > 4) {
      throw new IllegalArgumentException(
          "The number of bytes used to store small integers must be comprised between 1 and 4");
    }
    this.n = n;
  }

  /** {@inheritDoc} */
  @Override
  public int size(Integer value) {
    return n;
  }

  /** {@inheritDoc} */
  @Override
  public void write(ByteBuffer buffer, int position, Integer value) {
    for (int i = 0; i < n; i++) {
      buffer.put(position + i, (byte) (value >> (i << 3)));
    }
  }

  /** {@inheritDoc} */
  @Override
  public Integer read(ByteBuffer buffer, int position) {
    byte s = (byte) (buffer.get(position + n - 1) >= 0 ? 0 : -1);
    int l = 0;
    for (int i = 3; i > n - 1; i--) {
      l |= ((int) s & 0xff) << (i << 3);
    }
    for (int i = n - 1; i >= 0; i--) {
      l |= ((int) buffer.get(position + i) & 0xff) << (i << 3);
    }
    return l;
  }
}
