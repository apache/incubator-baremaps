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



import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Map;

/**
 * A map of data backed by a {@link DataStore} and whose keys are stored in an
 * {@link Long2LongOpenHashMap}.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 *
 * <p>
 * Copyright (c) Planetiler.
 */
public class LongDataOpenHashMap<T> implements LongDataMap<T> {

  private final Map<Long, Long> map;
  private final DataStore<T> store;

  public LongDataOpenHashMap(DataStore<T> values) {
    this.map = new Long2LongOpenHashMap();
    this.store = values;
  }

  @Override
  public void put(long key, T value) {
    map.put(key, store.add(value));
  }

  @Override
  public T get(long key) {
    return store.get(map.get(key));
  }
}
