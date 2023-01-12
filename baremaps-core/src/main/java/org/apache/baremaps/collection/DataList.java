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



import java.util.AbstractCollection;
import java.util.Iterator;

public abstract class DataList<T> extends AbstractCollection<T> {

  public abstract long append(T value);

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(T value) {
    append(value);
    return true;
  }

  public abstract void set(long index, T value);

  public abstract T get(long index);

  public abstract long sizeAsLong();

  public abstract void clear();

  public int size() {
    return (int) Math.min(sizeAsLong(), Integer.MAX_VALUE);
  }

  @Override
  public Iterator<T> iterator() {
    return new Iter();
  }

  private class Iter implements Iterator<T> {
    private long index = 0;

    private long size = sizeAsLong();

    @Override
    public boolean hasNext() {
      return index < size;
    }

    @Override
    public T next() {
      return get(index++);
    }
  }
}
