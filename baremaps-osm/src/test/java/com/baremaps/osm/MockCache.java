/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.osm;

import com.baremaps.osm.cache.Cache;
import java.util.ArrayList;
import java.util.List;

public class MockCache<T> implements Cache<Long, T> {

  private final List<T> nodes;

  public MockCache(List<T> nodes) {
    this.nodes = nodes;
  }

  @Override
  public T get(Long key) {
    return nodes.get(key.intValue());
  }

  @Override
  public List<T> getAll(List<Long> keys) {
    List<T> coordinateList = new ArrayList<>();
    for (Long key : keys) {
      coordinateList.add(get(key));
    }
    return coordinateList;
  }

  @Override
  public void put(Long key, T values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(List<Entry<Long, T>> storeEntries) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Long key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAll(List<Long> keys) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void importAll(List<Entry<Long, T>> values) {
    throw new UnsupportedOperationException();
  }

};
