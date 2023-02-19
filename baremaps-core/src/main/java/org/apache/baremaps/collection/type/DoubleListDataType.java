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

/** A {@link DataType} for reading and writing lists of doubles in {@link ByteBuffer}s. */
public class DoubleListDataType implements DataType<List<Double>> {

  /** {@inheritDoc} */
  @Override
  public int size(List<Double> values) {
    return Integer.BYTES + values.size() * Double.BYTES;
  }

  /** {@inheritDoc} */
  @Override
  public int size(ByteBuffer buffer, int position) {
    return buffer.getInt(position);
  }

  /** {@inheritDoc} */
  @Override
  public void write(ByteBuffer buffer, int position, List<Double> values) {
    buffer.putInt(position, size(values));
    position += Integer.BYTES;
    for (Double value : values) {
      buffer.putDouble(position, value);
      position += Double.BYTES;
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<Double> read(ByteBuffer buffer, int position) {
    var size = buffer.getInt(position);
    var list = new ArrayList<Double>(size);
    for (var p = position + Integer.BYTES; p < position + size; p += Double.BYTES) {
      list.add(buffer.getDouble(p));
    }
    return list;
  }
}
