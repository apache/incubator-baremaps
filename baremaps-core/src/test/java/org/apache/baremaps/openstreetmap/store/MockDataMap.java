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

package org.apache.baremaps.openstreetmap.store;



import java.util.Iterator;
import java.util.Map;
import org.apache.baremaps.collection.DataMap;
import org.jetbrains.annotations.Nullable;

public class MockDataMap<T> extends DataMap<T> {

  private final Map<Long, T> values;

  public MockDataMap(Map<Long, T> values) {
    this.values = values;
  }

  @Override
  protected Iterator<Long> keyIterator() {
    return values.keySet().iterator();
  }

  @Override
  protected Iterator<T> valueIterator() {
    return values.values().iterator();
  }

  @Override
  protected Iterator<Entry<Long, T>> entryIterator() {
    return values.entrySet().iterator();
  }

  @Override
  public long sizeAsLong() {
    return values.size();
  }

  @Override
  public boolean containsKey(Object key) {
    return values.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return values.containsValue(value);
  }

  @Override
  public T get(Object key) {
    return values.get(key);
  }

  @Nullable
  @Override
  public T put(Long key, T value) {
    return values.put(key, value);
  }

  @Override
  public T remove(Object key) {
    return values.remove(key);
  }

  @Override
  public void clear() {
    values.clear();
  }
}
