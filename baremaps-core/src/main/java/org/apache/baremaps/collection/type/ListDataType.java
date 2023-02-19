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
 * A {@link DataType} for reading and writing lists of objects in {@link ByteBuffer}s.
 */
public class ListDataType<T> implements DataType<List<T>> {

  public final DataType<T> dataType;

  /**
   * Constructs a {@link ListDataType} with a data type.
   *
   * @param dataType the data type of the values
   */
  public ListDataType(DataType<T> dataType) {
    this.dataType = dataType;
  }

  /** {@inheritDoc} */
  @Override
  public int size(List<T> values) {
    int size = Integer.BYTES;
    for (T value : values) {
      size += dataType.size(value);
    }
    return size;
  }

  @Override
  public int size(ByteBuffer buffer, int position) {
    return buffer.getInt(position);
  }

  /** {@inheritDoc} */
  @Override
  public void write(ByteBuffer buffer, int position, List<T> values) {
    buffer.putInt(position, size(values));
    position += Integer.BYTES;
    for (T value : values) {
      dataType.write(buffer, position, value);
      position += dataType.size(value);
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<T> read(ByteBuffer buffer, int position) {
    var size = buffer.getInt(position);
    var list = new ArrayList<T>(size);
    for (var p = position + Integer.BYTES; p < position + size; p += dataType.size(buffer, p)) {
      list.add(dataType.read(buffer, p));
    }
    return list;
  }
}
