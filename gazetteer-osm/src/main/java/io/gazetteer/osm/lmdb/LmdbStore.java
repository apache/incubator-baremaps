package io.gazetteer.osm.lmdb;

import io.gazetteer.osm.model.DataStore;
import io.gazetteer.osm.model.DataStoreException;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class LmdbStore<K, V> implements DataStore<K, V> {

  private final Env<ByteBuffer> env;

  private final Dbi<ByteBuffer> database;

  private final ObjectType<K, V> objectType;

  public LmdbStore(Env<ByteBuffer> env, Dbi<ByteBuffer> database, ObjectType<K, V> objectType) {
    checkNotNull(env);
    checkNotNull(database);
    checkNotNull(objectType);
    this.env = env;
    this.database = database;
    this.objectType = objectType;
  }

  @Override
  public void add(V value) throws DataStoreException {
    try {
      K key = objectType.ext(value);
      database.put(objectType.key(key), objectType.val(value));
    } catch (Exception e) {
      throw new DataStoreException(e);
    }
  }

  @Override
  public void addAll(Collection<V> values) throws DataStoreException {
    try (Txn<ByteBuffer> txn = env.txnWrite()) {
      for (V value : values) {
        K key = objectType.ext(value);
        database.put(txn, objectType.key(key), objectType.val(value));
      }
      txn.commit();
    } catch (Exception e) {
      throw new DataStoreException(e);
    }

  }

  @Override
  public V get(K id) throws DataStoreException {
    try (Txn<ByteBuffer> txn = env.txnRead()) {
      return objectType.val(database.get(txn, objectType.key(id)));
    } catch (Exception e) {
      throw new DataStoreException(e);
    }
  }

  @Override
  public List<V> getAll(List<K> ids) throws DataStoreException {
    try (Txn<ByteBuffer> txn = env.txnRead()) {
      List<V> values = new ArrayList<>();
      for (K id : ids) {
        values.add(objectType.val(database.get(txn, objectType.key(id))));
      }
      return values;
    } catch (Exception e) {
      throw new DataStoreException(e);
    }
  }

  @Override
  public void delete(K id) throws DataStoreException {
    try {
      database.delete(objectType.key(id));
    } catch (Exception e) {
      throw new DataStoreException(e);
    }
  }

  @Override
  public void deleteAll(List<K> ids) throws DataStoreException {
    try (Txn<ByteBuffer> txn = env.txnWrite()) {
      for (K id : ids) {
        database.delete(txn, objectType.key(id));
      }
    } catch (Exception e) {
      throw new DataStoreException(e);
    }
  }

  @Override
  public void close() {}

}
