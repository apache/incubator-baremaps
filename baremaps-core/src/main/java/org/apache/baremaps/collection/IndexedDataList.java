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



public class IndexedDataList<T> extends DataList<T> {

  private final MemoryAlignedDataList<Long> index;

  private final AppendOnlyBuffer<T> values;

  public IndexedDataList(MemoryAlignedDataList<Long> index, AppendOnlyBuffer<T> values) {
    this.index = index;
    this.values = values;
  }

  @Override
  public long append(T value) {
    long position = values.append(value);
    return index.append(position);
  }

  @Override
  public void set(long index, T value) {
    long position = values.append(value);
    this.index.set(index, position);
  }

  @Override
  public T get(long index) {
    long position = this.index.get(index);
    return values.get(position);
  }

  @Override
  public long sizeAsLong() {
    return index.sizeAsLong();
  }

  @Override
  public void clear() {
    index.clear();
    values.clear();
  }
}
