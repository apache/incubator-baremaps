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

package org.apache.baremaps.collection.sort;



import java.io.IOException;
import org.apache.baremaps.collection.DataList;

/**
 * A wrapper on top of a {@link DataList} which keeps the last data record in memory.
 *
 * @param <T>
 */
final class DataStack<T> implements AutoCloseable {

  private DataList<T> list;

  private Long index = 0l;

  private T cache;

  public DataStack(DataList<T> list) {
    this.list = list;
    reload();
  }

  public void close() throws IOException {
    list.close();
  }

  public boolean empty() {
    return this.index > list.size();
  }

  public T peek() {
    return this.cache;
  }

  public T pop() {
    T answer = peek(); // make a copy
    reload();
    return answer;
  }

  private void reload() {
    this.cache = this.list.get(index);
    index++;
  }
}
