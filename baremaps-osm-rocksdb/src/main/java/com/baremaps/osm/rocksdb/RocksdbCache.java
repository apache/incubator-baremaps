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
import com.baremaps.osm.cache.DataType;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

/** A {@code Cache} baked by RocksDB. */
public class RocksdbCache<K, V> implements Cache<K, V> {

  private final RocksDB db;

  private final DataType<K> keyType;

  private final DataType<V> valueType;

  public RocksdbCache(RocksDB db, DataType<K> keyType, DataType<V> valueType) {
    checkNotNull(db);
    this.db = db;
    this.keyType = keyType;
    this.valueType = valueType;
  }

  /** {@inheritDoc} */
  @Override
  public void add(K key, V value) throws CacheException {
    try {
      db.put(buffer(keyType, key), buffer(valueType, value));
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void add(List<Entry<K, V>> entries) throws CacheException {
    try (WriteBatch writeBatch = new WriteBatch()) {
      for (Entry<K, V> entry : entries) {
        K key = entry.key();
        V value = entry.value();
        writeBatch.put(buffer(keyType, key), buffer(valueType, value));
      }
      db.write(new WriteOptions(), writeBatch);
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void delete(K key) throws CacheException {
    try {
      db.delete(buffer(keyType, key));
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void delete(List<K> keys) throws CacheException {
    try (WriteBatch writeBatch = new WriteBatch()) {
      for (K key : keys) {
        writeBatch.delete(buffer(keyType, key));
      }
      db.write(new WriteOptions(), writeBatch);
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public V get(K key) throws CacheException {
    try {
      byte[] value = db.get(buffer(keyType, key));
      if (value == null) {
        return null;
      }
      return valueType.read(ByteBuffer.wrap(value));
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<V> get(List<K> keys) throws CacheException {
    try {
      List<byte[]> values =
          db.multiGetAsList(
              keys.stream().map(key -> buffer(keyType, key)).collect(Collectors.toList()));
      return values.stream()
          .map(
              value -> {
                if (value == null) {
                  return null;
                }
                return valueType.read(ByteBuffer.wrap(value));
              })
          .collect(Collectors.toList());
    } catch (RocksDBException e) {
      throw new CacheException(e);
    }
  }

  private <T> byte[] buffer(DataType<T> dataType, T value) {
    ByteBuffer buffer = ByteBuffer.allocate(dataType.size(value));
    dataType.write(buffer, value);
    return buffer.array();
  }
}
