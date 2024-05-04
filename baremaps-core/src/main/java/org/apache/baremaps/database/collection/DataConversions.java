package org.apache.baremaps.database.collection;

import java.util.*;
import java.util.Map.Entry;

public class DataConversions {

    public static <K, V> Map<K, V> asMap(DataMap<K, V> dataMap) {
        if (dataMap instanceof DataMapAdapter<K, V> adapter) {
            return adapter.map;
        } else {
            return new MapAdapter<>(dataMap);
        }
    }

    public static <K, V> DataMap<K, V> asDataMap(Map<K, V> map) {
        if (map instanceof MapAdapter<K, V> adapter) {
            return adapter.map;
        } else {
            return new DataMapAdapter<>(map);
        }
    }

    public static class MapAdapter<K, V> extends AbstractMap<K, V> {

        private final DataMap<K, V> map;
        private final int size;

        public MapAdapter(DataMap<K, V> dataMap) {
            this.map = dataMap;
            this.size = (int) dataMap.size();
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

    public static class DataMapAdapter<K, V> implements DataMap<K, V> {

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
        public V remove(K key) {
            return map.remove(key);
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
