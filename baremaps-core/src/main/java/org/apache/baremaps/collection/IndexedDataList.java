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



import java.io.IOException;

public class IndexedDataList<T> implements DataList<T> {

  private final LongList index;

  private final DataStore<T> store;

  public IndexedDataList(LongList index, DataStore<T> store) {
    this.index = index;
    this.store = store;
  }

  @Override
  public long add(T value) {
    return index.add(store.add(value));
  }

  @Override
  public void add(long idx, T value) {
    index.add(idx, store.add(value));
  }

  @Override
  public T get(long idx) {
    return store.get(index.get(idx));
  }

  @Override
  public long size() {
    return index.size();
  }

  @Override
  public void close() throws IOException {
    index.close();
    store.close();
  }

  @Override
  public void clean() throws IOException {
    index.clean();
    store.clean();
  }
}
