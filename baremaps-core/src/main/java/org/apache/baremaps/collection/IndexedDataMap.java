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

package org.apache.baremaps.collection;



import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Iterator;
import java.util.Map;

public class IndexedDataMap<T> extends DataMap<T> {

  private final Map<Long, Long> index;

  private final AppendOnlyBuffer<T> store;

  public IndexedDataMap(AppendOnlyBuffer<T> store) {
    this(new Long2LongOpenHashMap(), store);
  }

  public IndexedDataMap(Map<Long, Long> index, AppendOnlyBuffer<T> store) {
    this.index = index;
    this.store = store;
  }

  @Override
  public T put(Long key, T value) {
    var oldIndex = index.get(key);
    var position = store.append(value);
    index.put(key, position);
    return oldIndex == null ? null : store.get(oldIndex);
  }

  @Override
  public T get(Object key) {
    var position = index.get(key);
    return position == null ? null : store.get(position);
  }

  @Override
  protected Iterator<Long> keyIterator() {
    return index.keySet().iterator();
  }

  @Override
  protected Iterator<T> valueIterator() {
    return Streams.stream(keyIterator()).map(this::get).iterator();
  }

  @Override
  protected Iterator<Entry<Long, T>> entryIterator() {
    return Streams.stream(keyIterator()).map(this::entryV).iterator();
  }

  private Entry<Long, T> entryV(long key) {
    return new Entry() {
      @Override
      public Long getKey() {
        return key;
      }

      @Override
      public T getValue() {
        return get(key);
      }

      @Override
      public T setValue(Object value) {
        return put(key, (T) value);
      }
    };
  }

  @Override
  public boolean isEmpty() {
    return index.isEmpty();
  }

  @Override
  public long sizeAsLong() {
    if (index instanceof DataMap<Long> dataMap) {
      return dataMap.sizeAsLong();
    } else {
      return index.size();
    }
  }

  @Override
  public boolean containsKey(Object key) {
    return index.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return index.values().stream().map(store::get).anyMatch(value::equals);
  }

  @Override
  public T remove(Object key) {
    return store.get(index.remove(key));
  }

  @Override
  public void clear() {
    index.clear();
    store.clear();
  }
}
