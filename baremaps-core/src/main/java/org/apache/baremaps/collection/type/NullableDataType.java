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

public class NullableDataType<T> implements DataType<T> {

  private final DataType<T> dataType;

  public NullableDataType(DataType<T> dataType) {
    this.dataType = dataType;
  }

  @Override
  public int size(T value) {
    return 1 + dataType.size(value);
  }

  @Override
  public void write(ByteBuffer buffer, int position, T value) {
    if (value == null) {
      buffer.put(position, (byte) 0);
    } else {
      buffer.put(position, (byte) 1);
      dataType.write(buffer, position + 1, value);
    }
  }

  @Override
  public T read(ByteBuffer buffer, int position) {
    if (buffer.get(position) == 0) {
      return null;
    } else {
      return dataType.read(buffer, position + 1);
    }
  }
}
