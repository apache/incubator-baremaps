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
import java.util.ArrayList;
import java.util.List;

/** A {@link DataType} for reading and writing lists of shorts in {@link ByteBuffer}s. */
public class ShortListDataType implements DataType<List<Short>> {

  /** {@inheritDoc} */
  @Override
  public int size(List<Short> values) {
    return Integer.BYTES + values.size() * Short.BYTES;
  }

  /** {@inheritDoc} */
  @Override
  public int size(ByteBuffer buffer, int position) {
    return buffer.getInt(position);
  }

  /** {@inheritDoc} */
  @Override
  public void write(ByteBuffer buffer, int position, List<Short> values) {
    buffer.putInt(position, size(values));
    position += Integer.BYTES;
    for (Short value : values) {
      buffer.putShort(position, value);
      position += Short.BYTES;
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<Short> read(ByteBuffer buffer, int position) {
    var size = buffer.getInt(position);
    var list = new ArrayList<Short>(size);
    for (var p = position + Integer.BYTES; p < position + size; p += Short.BYTES) {
      list.add(buffer.getShort(p));
    }
    return list;
  }
}
