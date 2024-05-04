/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.database.collection;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public class MapAdapter<K, V> extends AbstractMap<K, V> {

  private final DataMap<K, V> dataMap;
  private final int size;

  public MapAdapter(DataMap<K, V> dataMap) {
    this.dataMap = dataMap;
    this.size = (int) dataMap.size();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return new AbstractSet<>() {
      @Override
      public Iterator<Entry<K, V>> iterator() {
        return dataMap.entryIterator();
      }

      @Override
      public int size() {
        return size;
      }
    };
  }
}
