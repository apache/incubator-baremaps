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

/** A {@link DataType} for reading and writing lists of doubles in {@link ByteBuffer}s. */
public class DoubleArrayDataType implements DataType<double[]> {

  /** {@inheritDoc} */
  @Override
  public int size(final double[] values) {
    return Integer.BYTES + values.length * Double.BYTES;
  }

  /** {@inheritDoc} */
  @Override
  public int size(final ByteBuffer buffer, final int position) {
    return buffer.getInt(position);
  }

  /** {@inheritDoc} */
  @Override
  public void write(final ByteBuffer buffer, final int position, final double[] values) {
    var p = position;
    buffer.putInt(p, size(values));
    p += Integer.BYTES;
    for (double value : values) {
      buffer.putDouble(p, value);
      p += Double.BYTES;
    }
  }

  /** {@inheritDoc} */
  @Override
  public double[] read(final ByteBuffer buffer, final int position) {
    var size = buffer.getInt(position);
    var length = (size - Integer.BYTES) / Double.BYTES;
    var list = new double[length];
    var index = 0;
    for (var p = position + Integer.BYTES; p < position + size; p += Double.BYTES) {
      list[index] = buffer.getDouble(p);
      index++;
    }
    return list;
  }
}
