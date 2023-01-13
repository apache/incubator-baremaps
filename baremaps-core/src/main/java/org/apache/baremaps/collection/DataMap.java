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
import java.util.*;

public abstract class DataMap<T> implements Map<Long, T> {

  @Override
  public void putAll(Map<? extends Long, ? extends T> m) {
    m.forEach(this::put);
  }

  public List<T> getAll(List<Long> keys) {
    return Streams.stream(keys).map(this::get).toList();
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  public abstract long sizeAsLong();

  public int size() {
    return (int) Math.min(sizeAsLong(), Integer.MAX_VALUE);
  }

  protected abstract Iterator<Long> keyIterator();

  @Override
  public Set<Long> keySet() {
    return new KeySet();
  }

  private class KeySet extends AbstractSet<Long> {
    @Override
    public Iterator<Long> iterator() {
      return keyIterator();
    }

    @Override
    public int size() {
      return DataMap.this.size();
    }
  }

  protected abstract Iterator<T> valueIterator();

  @Override
  public Collection<T> values() {
    return new ValueCollection();
  }

  private class ValueCollection extends AbstractCollection<T> {
    @Override
    public Iterator<T> iterator() {
      return valueIterator();
    }

    @Override
    public int size() {
      return DataMap.this.size();
    }
  }

  protected abstract Iterator<Entry<Long, T>> entryIterator();

  @Override
  public Set<Entry<Long, T>> entrySet() {
    return new EntrySet();
  }

  private class EntrySet extends AbstractSet<Entry<Long, T>> {
    @Override
    public Iterator<Entry<Long, T>> iterator() {
      return entryIterator();
    }

    @Override
    public int size() {
      return DataMap.this.size();
    }
  }
}
