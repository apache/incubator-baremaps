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

package com.baremaps.osm.lmdb;

import static com.google.common.base.Preconditions.checkNotNull;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.CacheMapper;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

/** A {@code Cache} baked by LMDB. */
public class LmdbCache<K, V> implements Cache<K, V> {

  private final Env<ByteBuffer> env;

  private final Dbi<ByteBuffer> database;

  private final CacheMapper<K> keyType;

  private final CacheMapper<V> valueType;

  public LmdbCache(
      Env<ByteBuffer> env,
      Dbi<ByteBuffer> database,
      CacheMapper<K> keyType,
      CacheMapper<V> valueType) {
    checkNotNull(env);
    checkNotNull(database);
    this.env = env;
    this.database = database;
    this.keyType = keyType;
    this.valueType = valueType;
  }

  /** {@inheritDoc} */
  @Override
  public void put(K key, V value) {
    try (Txn<ByteBuffer> txn = env.txnWrite()) {
      database.put(txn, buffer(keyType, key), buffer(valueType, value));
      txn.commit();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void put(List<Entry<K, V>> entries) {
    try (Txn<ByteBuffer> txn = env.txnWrite()) {
      for (Entry<K, V> entry : entries) {
        K key = entry.key();
        V value = entry.value();
        database.put(txn, buffer(keyType, key), buffer(valueType, value));
      }
      txn.commit();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void delete(K key) {
    try (Txn<ByteBuffer> txn = env.txnWrite()) {
      database.delete(txn, buffer(keyType, key));
      txn.commit();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void delete(List<K> keys) {
    try (Txn<ByteBuffer> txn = env.txnWrite()) {
      for (K key : keys) {
        database.delete(txn, buffer(keyType, key));
      }
      txn.commit();
    }
  }

  /** {@inheritDoc} */
  @Override
  public V get(K key) {
    try (Txn<ByteBuffer> txn = env.txnRead()) {
      ByteBuffer buffer = database.get(txn, buffer(keyType, key));
      return valueType.read(buffer);
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<V> get(List<K> keys) {
    List<V> list = new ArrayList<>();
    try (Txn<ByteBuffer> txn = env.txnRead()) {
      for (K key : keys) {
        ByteBuffer buffer = database.get(txn, buffer(keyType, key));
        list.add(valueType.read(buffer));
      }
    }
    return list;
  }

  private <T> ByteBuffer buffer(CacheMapper<T> cacheMapper, T value) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(cacheMapper.size(value));
    cacheMapper.write(buffer, value);
    return buffer;
  }
}
