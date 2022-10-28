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



import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.baremaps.collection.LongDataMap;

public class MockLongDataMap<T> implements LongDataMap<T> {

  private final Map<Long, T> values;

  public MockLongDataMap(Map<Long, T> values) {
    this.values = values;
  }

  public List<T> get(List<Long> keys) {
    List<T> coordinateList = new ArrayList<>();
    for (Long key : keys) {
      coordinateList.add(get(key));
    }
    return coordinateList;
  }

  @Override
  public void put(long key, T value) {}

  @Override
  public T get(long key) {
    return values.get(key);
  }
}
