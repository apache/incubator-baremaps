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

package org.apache.baremaps.collection.store;



import java.io.Closeable;
import java.util.Iterator;
import org.apache.baremaps.collection.Cleanable;

public interface DataStore<T> extends Iterable<T>, Closeable, Cleanable {

  long add(T value);

  void set(long index, T value);

  T get(long index);

  long size();

  @Override
  default Iterator<T> iterator() {
    final long size = size();
    return new Iterator<T>() {

      private long index = 0;

      @Override
      public boolean hasNext() {
        return index < size;
      }

      @Override
      public T next() {
        return get(index++);
      }
    };
  }

}
