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

/** A {@link DataType} for reading and writing lists of objects in {@link ByteBuffer}s. */
public class ListDataType<T> implements DataType<List<T>> {

  public final DataType<T> dataType;

  public ListDataType(DataType<T> dataType) {
    this.dataType = dataType;
  }

  /** {@inheritDoc} */
  @Override
  public int size(List<T> values) {
    int size = 4;
    for (T value : values) {
      size += dataType.size(value);
    }
    return size;
  }

  /** {@inheritDoc} */
  @Override
  public void write(ByteBuffer buffer, int position, List<T> values) {
    buffer.putInt(position, values.size());
    for (T value : values) {
      position += dataType.size(value);
      dataType.write(buffer, position, value);
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<T> read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    position += 4;
    List<T> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      T value = dataType.read(buffer, position);
      position += dataType.size(value);
      list.add(value);
    }
    return list;
  }
}
