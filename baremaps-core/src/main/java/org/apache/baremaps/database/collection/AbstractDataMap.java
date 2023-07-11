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

package org.apache.baremaps.database.collection;



import java.util.*;

/**
 * An abstract map of data elements that can hold a large number of elements.
 *
 * @param <E> The type of the elements.
 */
public abstract class AbstractDataMap<E> implements DataMap<E> {

  /** {@inheritDoc} */
  @Override
  public void putAll(Map<? extends Long, ? extends E> m) {
    m.forEach(this::put);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns an iterator over the keys of the map.
   *
   * @return an iterator
   */
  protected abstract Iterator<Long> keyIterator();

  /** {@inheritDoc} */
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
      return AbstractDataMap.this.size();
    }
  }

  /**
   * Returns an iterator over the values of the map.
   *
   * @return an iterator
   */
  protected abstract Iterator<E> valueIterator();

  /** {@inheritDoc} */
  @Override
  public Collection<E> values() {
    return new ValueCollection();
  }

  private class ValueCollection extends AbstractCollection<E> {
    @Override
    public Iterator<E> iterator() {
      return valueIterator();
    }

    @Override
    public int size() {
      return AbstractDataMap.this.size();
    }
  }

  /**
   * Returns an iterator over the entries of the map.
   *
   * @return an iterator
   */
  protected abstract Iterator<Entry<Long, E>> entryIterator();

  /** {@inheritDoc} */
  @Override
  public Set<Entry<Long, E>> entrySet() {
    return new EntrySet();
  }

  private class EntrySet extends AbstractSet<Entry<Long, E>> {
    @Override
    public Iterator<Entry<Long, E>> iterator() {
      return entryIterator();
    }

    @Override
    public int size() {
      return AbstractDataMap.this.size();
    }
  }
}
