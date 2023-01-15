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

/**
 * A {@link DataType} for reading and writing lists of bytes in {@link ByteBuffer}s.
 */
public class ByteListDataType implements DataType<List<Byte>> {

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(List<Byte> values) {
    return Integer.BYTES + values.size() * Byte.BYTES;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(ByteBuffer buffer, int position) {
    return buffer.getInt(position);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(ByteBuffer buffer, int position, List<Byte> values) {
    buffer.putInt(position, size(values));
    position += Integer.BYTES;
    for (Byte value : values) {
      buffer.put(position, value);
      position += Byte.BYTES;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Byte> read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    var list = new ArrayList<Byte>(size);
    for (var p = position + Integer.BYTES; p < position + size; p += Byte.BYTES) {
      list.add(buffer.get(p));
    }
    return list;
  }
}
