package io.gazetteer.osm.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DataStore<K, V> extends AutoCloseable {

    void add(V value) throws DataStoreException;

    void addAll(Collection<V> values) throws DataStoreException;

    V get(K id) throws DataStoreException;

    List<V> getAll(List<K> ids) throws DataStoreException;

    void delete(K id) throws DataStoreException;

    void deleteAll(List<K> ids) throws DataStoreException;

}
