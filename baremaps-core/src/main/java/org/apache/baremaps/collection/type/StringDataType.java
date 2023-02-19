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
import java.nio.charset.StandardCharsets;

/**
 * A {@link DataType} for reading and writing strings in {@link ByteBuffer}s.
 */
public class StringDataType implements DataType<String> {

  /** {@inheritDoc} */
  @Override
  public int size(String value) {
    return Integer.BYTES + value.getBytes(StandardCharsets.UTF_8).length;
  }

  /** {@inheritDoc} */
  @Override
  public int size(ByteBuffer buffer, int position) {
    return buffer.getInt(position);
  }

  /** {@inheritDoc} */
  @Override
  public void write(ByteBuffer buffer, int position, String value) {
    var bytes = value.getBytes(StandardCharsets.UTF_8);
    buffer.putInt(position, size(value));
    buffer.put(position + Integer.BYTES, bytes, 0, bytes.length);
  }

  /** {@inheritDoc} */
  @Override
  public String read(ByteBuffer buffer, int position) {
    var size = size(buffer, position);
    var bytes = new byte[Math.max(size - Integer.BYTES, 0)];
    buffer.get(position + Integer.BYTES, bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }
}
