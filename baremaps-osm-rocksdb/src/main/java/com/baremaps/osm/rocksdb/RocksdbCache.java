package com.baremaps.osm.rocksdb;

import static com.google.common.base.Preconditions.checkNotNull;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.CacheException;
import java.util.ArrayList;
import java.util.List;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public abstract class RocksdbCache<K, V> implements Cache<K, V> {

  private final RocksDB db;

  protected RocksdbCache(RocksDB db) {
    checkNotNull(db);
    this.db = db;
  }

  public void add(K key, V value) throws CacheException {
    try {
      db.put(buffer(key), write(value));
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  public void add(List<Entry<K, V>> entries) throws CacheException {
    for (Entry<K, V> entry : entries) {
      add(entry.key(), entry.value());
    }
  }

  @Override
  public void delete(K key) throws CacheException {
    try {
      db.delete(buffer(key));
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  @Override
  public void deleteAll(List<K> keys) throws CacheException {
    for (K key : keys) {
      delete(key);
    }
  }

  public V get(K key) throws CacheException {
    try {
      return read(db.get(buffer(key)));
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  public List<V> get(List<K> keys) throws CacheException {
    List<V> list = new ArrayList<>();
    for (K key : keys) {
      list.add(get(key));
    }
    return list;
  }

  public abstract byte[] buffer(K key);

  public abstract byte[] write(V t);

  public abstract V read(byte[] buffer);


}
