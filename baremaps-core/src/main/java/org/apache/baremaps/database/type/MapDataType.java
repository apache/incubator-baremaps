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
import java.util.HashMap;
import java.util.Map;

public class MapDataType<K, V> implements DataType<Map<K, V>> {

  private final DataType<K> keyType;

  private final DataType<V> valueType;

  public MapDataType(final DataType<K> keyType, final DataType<V> valueType) {
    this.keyType = keyType;
    this.valueType = valueType;
  }

  @Override
  public int size(final Map<K, V> value) {
    int size = Integer.BYTES;
    for (Map.Entry<K, V> entry : value.entrySet()) {
      size += keyType.size(entry.getKey());
      size += valueType.size(entry.getValue());
    }
    return size;
  }

  @Override
  public int size(final ByteBuffer buffer, final int position) {
    return buffer.getInt(position);
  }

  @Override
  public void write(final ByteBuffer buffer, final int position, final Map<K, V> value) {
    buffer.putInt(position, size(value));
    int p = position + Integer.BYTES;
    for (Map.Entry<K, V> entry : value.entrySet()) {
      keyType.write(buffer, p, entry.getKey());
      p += keyType.size(entry.getKey());
      valueType.write(buffer, p, entry.getValue());
      p += valueType.size(entry.getValue());
    }
  }

  @Override
  public Map<K, V> read(final ByteBuffer buffer, final int position) {
    int size = buffer.getInt(position);
    var map = new HashMap<K, V>(size);
    for (int p = position + Integer.BYTES; p < position + size;) {
      K key = keyType.read(buffer, p);
      p += keyType.size(key);
      V value = valueType.read(buffer, p);
      p += valueType.size(value);
      map.put(key, value);
    }
    return map;
  }
}
