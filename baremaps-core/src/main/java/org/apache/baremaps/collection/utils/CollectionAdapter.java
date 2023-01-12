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

package org.apache.baremaps.collection.utils;



import java.util.Collection;
import java.util.Iterator;
import org.apache.baremaps.collection.DataList;

public class CollectionAdapter<T> implements Collection<T> {

  private final DataList<T> dataList;

  public CollectionAdapter(DataList<T> dataList) {
    this.dataList = dataList;
  }

  @Override
  public int size() {
    if (dataList.sizeAsLong() > Integer.MAX_VALUE) {
      throw new IllegalStateException(
          "The collection is too large to be represented as an integer.");
    }
    return (int) dataList.sizeAsLong();
  }

  @Override
  public boolean isEmpty() {
    return dataList.sizeAsLong() == 0;
  }

  @Override
  public boolean contains(Object o) {
    for (long i = 0; i < dataList.sizeAsLong(); i++) {
      if (dataList.get(i).equals(o)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<>() {

      private long index = 0;

      @Override
      public boolean hasNext() {
        return index < dataList.sizeAsLong();
      }

      @Override
      public T next() {
        return dataList.get(index++);
      }
    };
  }

  @Override
  public Object[] toArray() {
    return toArray(new Object[size()]);
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    for (int i = 0; i < size(); i++) {
      a[i] = (T1) dataList.get(i);
    }
    return a;
  }

  @Override
  public boolean add(T t) {
    dataList.append(t);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object o : c) {
      if (!contains(o)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    for (T t : c) {
      add(t);
    }
    return true;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }
}
