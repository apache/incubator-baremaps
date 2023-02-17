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



import com.google.common.primitives.Bytes;
import java.nio.ByteBuffer;
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
    byte[] bytes = Bytes.toArray(values);
    buffer.put(position + Integer.BYTES, bytes, 0, bytes.length);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Byte> read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    var bytes = new byte[Math.max(size - Integer.BYTES, 0)];
    buffer.get(position + Integer.BYTES, bytes);
    return Bytes.asList(bytes);
  }
}
