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



import org.apache.baremaps.collection.store.AppendOnlyCollection;

public class IndexedLongMap<T> implements LongMap<T> {

  private final LongLongMap index;

  private final AppendOnlyCollection<T> store;

  public IndexedLongMap(LongLongMap index, AppendOnlyCollection<T> store) {
    this.index = index;
    this.store = store;
  }

  @Override
  public void put(long key, T value) {
    var position = store.append(value);
    index.put(key, position);
  }

  @Override
  public T get(long idx) {
    var position = index.get(idx);
    return store.read(position);
  }
}
