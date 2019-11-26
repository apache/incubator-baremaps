package io.gazetteer.osm.lmdb;

import static com.google.common.base.Preconditions.checkNotNull;

import io.gazetteer.osm.model.Store;
import io.gazetteer.osm.model.StoreEntry;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

public abstract class LmdbStore<K, V> implements Store<K, V> {

  private final Env<ByteBuffer> env;

  private final Dbi<ByteBuffer> database;

  public LmdbStore(Env<ByteBuffer> env, Dbi<ByteBuffer> database) {
    checkNotNull(env);
    checkNotNull(database);
    this.env = env;
    this.database = database;
  }


  public void put(K key, V value) {
    try (Txn<ByteBuffer> txn = env.txnWrite()) {
      database.put(txn, buffer(key), write(value));
      txn.commit();
    }
  }

  @Override
  public void delete(K key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAll(List<K> keys) {
    throw new UnsupportedOperationException();
  }

  public void putAll(List<StoreEntry<K, V>> entries) {
    try (Txn<ByteBuffer> txn = env.txnWrite()) {
      for (StoreEntry<K, V> entry : entries) {
        database.put(txn, buffer(entry.key()), write(entry.value()));
      }
      txn.commit();
    }
  }

  public V get(K key) {
    try (Txn<ByteBuffer> txn = env.txnRead()) {
      ByteBuffer buffer = database.get(txn, buffer(key));
      return read(buffer);
    }
  }

  public List<V> getAll(List<K> keys) {
    List<V> list = new ArrayList<>();
    try (Txn<ByteBuffer> txn = env.txnRead()) {
      for (K key : keys) {
        ByteBuffer buffer = database.get(txn, buffer(key));
        list.add(read(buffer));
      }
    }
    return list;
  }

  @Override
  public void importAll(List<StoreEntry<K, V>> values) {
    throw new UnsupportedOperationException();
  }

  public void close() {
    database.close();
  }

  public abstract ByteBuffer buffer(K key);

  public abstract V read(ByteBuffer buffer);

  public abstract ByteBuffer write(V t);


}
