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

package org.apache.baremaps.database.type;



import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/** A {@link DataType} for reading and writing lists of floats in {@link ByteBuffer}s. */
public class FloatListDataType implements DataType<List<Float>> {

  /** {@inheritDoc} */
  @Override
  public int size(final List<Float> values) {
    return Integer.BYTES + values.size() * Float.BYTES;
  }

  /** {@inheritDoc} */
  @Override
  public int size(final ByteBuffer buffer, final int position) {
    return buffer.getInt(position);
  }

  /** {@inheritDoc} */
  @Override
  public void write(final ByteBuffer buffer, final int position, final List<Float> values) {
    buffer.putInt(position, size(values));
    var p = position + Integer.BYTES;
    for (float value : values) {
      buffer.putFloat(p, value);
      p += Float.BYTES;
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<Float> read(final ByteBuffer buffer, final int position) {
    int size = buffer.getInt(position);
    int length = (size - Integer.BYTES) / Float.BYTES;
    var values = new ArrayList<Float>(length);
    for (int index = 0; index < length; index++) {
      values.add(buffer.getFloat(position + Integer.BYTES + index * Float.BYTES));
    }
    return values;
  }
}
