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

package com.baremaps.osm.rocksdb;

import static com.google.common.base.Preconditions.checkNotNull;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.CacheException;
import java.util.List;
import java.util.stream.Collectors;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

public abstract class RocksdbCache<K, V> implements Cache<K, V> {

  private final RocksDB db;

  protected RocksdbCache(RocksDB db) {
    checkNotNull(db);
    this.db = db;
  }

  public void add(K key, V value) throws CacheException {
    try {
      db.put(key(key), write(value));
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  public void add(List<Entry<K, V>> entries) throws CacheException {
    try (WriteBatch writeBatch = new WriteBatch()) {
      for (Entry<K, V> entry : entries) {
        writeBatch.put(key(entry.key()), write(entry.value()));
      }
      db.write(new WriteOptions(), writeBatch);
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  @Override
  public void delete(K key) throws CacheException {
    try {
      db.delete(key(key));
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  @Override
  public void delete(List<K> keys) throws CacheException {
    try (WriteBatch writeBatch = new WriteBatch()) {
      for (K key : keys) {
        writeBatch.delete(key(key));
      }
      db.write(new WriteOptions(), writeBatch);
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  public V get(K key) throws CacheException {
    try {
      return read(db.get(key(key)));
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  public List<V> get(List<K> keys) throws CacheException {
    try {
      List<byte[]> values =
          db.multiGetAsList(keys.stream().map(k -> key(k)).collect(Collectors.toList()));
      return values.stream().map(v -> read(v)).collect(Collectors.toList());
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  public abstract byte[] key(K key);

  public abstract byte[] write(V t);

  public abstract V read(byte[] buffer);
}
