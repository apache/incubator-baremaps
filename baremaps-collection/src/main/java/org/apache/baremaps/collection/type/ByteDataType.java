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

/** A {@link DataType} for reading and writing bytes in {@link ByteBuffer}s. */
public class ByteDataType implements SizedDataType<Byte> {

  /** {@inheritDoc} */
  @Override
  public int size(Byte value) {
    return 1;
  }

  /** {@inheritDoc} */
  @Override
  public void write(ByteBuffer buffer, int position, Byte value) {
    buffer.put(position, value);
  }

  /** {@inheritDoc} */
  @Override
  public Byte read(ByteBuffer buffer, int position) {
    return buffer.get(position);
  }
}
