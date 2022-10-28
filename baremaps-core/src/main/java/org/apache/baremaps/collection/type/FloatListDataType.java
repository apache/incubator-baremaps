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

/** A {@link DataType} for reading and writing lists of floats in {@link ByteBuffer}s. */
public class FloatListDataType implements DataType<List<Float>> {

  /** {@inheritDoc} */
  @Override
  public int size(List<Float> values) {
    return 4 + values.size() * 4;
  }

  /** {@inheritDoc} */
  @Override
  public void write(ByteBuffer buffer, int position, List<Float> values) {
    buffer.putInt(position, values.size());
    position += 4;
    for (Float value : values) {
      buffer.putFloat(position, value);
      position += 4;
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<Float> read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    position += 4;
    List<Float> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add(buffer.getFloat(position));
      position += 4;
    }
    return list;
  }
}
