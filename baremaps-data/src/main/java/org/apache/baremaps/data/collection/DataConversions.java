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

package org.apache.baremaps.data.collection;

import java.util.*;
import java.util.Map.Entry;

/**
 * Utility class for converting between collections and data collections.
 */
public class DataConversions {

  private DataConversions() {
    // Utility class
  }

  /**
   * Converts a {@code DataCollection} to a {@code Collection}.
   * 
   * @param dataCollection
   * @return
   * @param <E>
   */
  public static <E> Collection<E> asCollection(DataCollection<E> dataCollection) {
    if (dataCollection instanceof DataCollectionAdapter<E>adapter) {
      return adapter.collection;
    } else {
      return new CollectionAdapter<>(dataCollection);
    }
  }

  /**
   * Converts a {@code Collection} to a {@code DataCollection}.
   *
   * @param collection
   * @return
   * @param <E>
   */
  public static <E> DataCollection<E> asDataCollection(Collection<E> collection) {
    if (collection instanceof CollectionAdapter<E>adapter) {
      return adapter.collection;
    } else {
      return new DataCollectionAdapter<>(collection);
    }
  }

  /**
   * Converts a {@code DataList} to a {@code List}.
   *
   * @param dataList the data list
   * @return the list
   */
  public static <E> List<E> asList(DataList<E> dataList) {
    if (dataList instanceof DataListAdapter<E>adapter) {
      return adapter.list;
    } else {
      return new ListAdapter<>(dataList);
    }
  }

  /**
   * Converts a {@code List} to a {@code DataList}.
   *
   * @param list the list
   * @return the data list
   */
  public static <E> DataList<E> asDataList(List<E> list) {
    if (list instanceof ListAdapter<E>adapter) {
      return adapter.list;
    } else {
      return new DataListAdapter<>(list);
    }
  }

  /**
   * Converts a {@code DataMap} to a {@code Map}.
   *
   * @param dataMap the data map
   * @return the map
   */
  public static <K, V> Map<K, V> asMap(DataMap<K, V> dataMap) {
    if (dataMap instanceof DataMapAdapter<K, V>adapter) {
      return adapter.map;
    } else {
      return new MapAdapter<>(dataMap);
    }
  }

  /**
   * Converts a {@code Map} to a {@code DataMap}.
   *
   * @param map the map
   * @return the data map
   */
  public static <K, V> DataMap<K, V> asDataMap(Map<K, V> map) {
    if (map instanceof MapAdapter<K, V>adapter) {
      return adapter.map;
    } else {
      return new DataMapAdapter<>(map);
    }
  }

  private static class CollectionAdapter<E> extends AbstractCollection<E> {

    private final DataCollection<E> collection;
    private final int size;

    public CollectionAdapter(DataCollection<E> dataCollection) {
      this.collection = dataCollection;
      this.size = (int) dataCollection.size();
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public Iterator<E> iterator() {
      return collection.iterator();
    }
  }

  private static class DataCollectionAdapter<E> implements DataCollection<E> {

    private final Collection<E> collection;

    public DataCollectionAdapter(Collection<E> collection) {
      this.collection = collection;
    }

    @Override
    public long size() {
      return collection.size();
    }

    @Override
    public boolean add(E value) {
      return collection.add(value);
    }

    @Override
    public void clear() {
      collection.clear();
    }

    @Override
    public Iterator<E> iterator() {
      return collection.iterator();
    }
  }

  private static class ListAdapter<E> extends AbstractList<E> {

    private final DataList<E> list;
    private final int size;

    public ListAdapter(DataList<E> dataList) {
      this.list = dataList;
      this.size = (int) dataList.size();
    }

    @Override
    public boolean add(E value) {
      return list.add(value);
    }

    @Override
    public E set(int index, E value) {
      var oldValue = list.get(index);
      list.set(index, value);
      return oldValue;
    }

    @Override
    public E get(int index) {
      return list.get(index);
    }

    @Override
    public int size() {
      return size;
    }
  }

  private static class DataListAdapter<E> implements DataList<E> {

    private final List<E> list;

    public DataListAdapter(List<E> list) {
      this.list = list;
    }

    @Override
    public long size() {
      return list.size();
    }

    @Override
    public void clear() {
      list.clear();
    }

    @Override
    public long addIndexed(E value) {
      list.add(value);
      return list.size() - 1L;
    }

    @Override
    public void set(long index, E value) {
      list.set((int) index, value);
    }

    @Override
    public E get(long index) {
      return list.get((int) index);
    }
  }

  private static class MapAdapter<K, V> extends AbstractMap<K, V> {

    private final DataMap<K, V> map;
    private final int size;

    public MapAdapter(DataMap<K, V> dataMap) {
      this.map = dataMap;
      this.size = (int) dataMap.size();
    }

    @Override
    public V put(K key, V value) {
      return map.put(key, value);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
      return new AbstractSet<>() {
        @Override
        public Iterator<Entry<K, V>> iterator() {
          return map.entryIterator();
        }

        @Override
        public int size() {
          return size;
        }
      };
    }
  }

  private static class DataMapAdapter<K, V> implements DataMap<K, V> {

    private final Map<K, V> map;

    public DataMapAdapter(Map<K, V> map) {
      this.map = map;
    }

    @Override
    public long size() {
      return map.size();
    }

    @Override
    public V get(Object key) {
      return map.get(key);
    }

    @Override
    public V put(K key, V value) {
      return map.put(key, value);
    }

    @Override
    public boolean containsKey(Object key) {
      return map.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
      return map.containsValue(value);
    }

    @Override
    public void clear() {
      map.clear();
    }

    @Override
    public Iterator<K> keyIterator() {
      return map.keySet().iterator();
    }

    @Override
    public Iterator<V> valueIterator() {
      return map.values().iterator();
    }

    @Override
    public Iterator<Entry<K, V>> entryIterator() {
      return map.entrySet().iterator();
    }
  }

}
