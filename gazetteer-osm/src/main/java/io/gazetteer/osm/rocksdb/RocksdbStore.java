package io.gazetteer.osm.rocksdb;

import io.gazetteer.osm.model.DataStore;
import io.gazetteer.osm.model.DataStoreException;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class RocksdbStore<K, V> implements DataStore<K, V> {

  static {
    RocksDB.loadLibrary();
  }

  private final RocksDB database;

  private final ColumnFamilyHandle column;

  private final ObjectType<K, V> objectType;

  private RocksdbStore(RocksDB database, ColumnFamilyHandle column, ObjectType<K, V> objectType) {
    checkNotNull(database);
    checkNotNull(objectType);
    this.database = database;
    this.column = column;
    this.objectType = objectType;
  }

  @Override
  public void add(V value) throws DataStoreException {
    try {
      K key = objectType.ext(value);
      database.put(column, objectType.key(key), objectType.val(value));
    } catch (Exception e) {
      throw new DataStoreException(e);
    }
  }

  @Override
  public void addAll(Collection<V> entries) throws DataStoreException {
    if (entries.size() > 0) {
      try (WriteBatch batch = new WriteBatch()) {
        for (V value : entries) {
          K key = objectType.ext(value);
          batch.put(column, objectType.key(key), objectType.val(value));
        }
        database.write(new WriteOptions(), batch);
      } catch (Exception e) {
        throw new DataStoreException(e);
      }
    }
  }

  @Override
  public V get(K id) throws DataStoreException {
    try {
      return objectType.val(database.get(column, objectType.key(id)));
    } catch (Exception e) {
      throw new DataStoreException(e);
    }
  }

  @Override
  public List<V> getAll(List<K> ids) throws DataStoreException {
    try {
      List<ColumnFamilyHandle> columns = new ArrayList<>();
      List<byte[]> keys = new ArrayList<>();
      for (K id : ids) {
        columns.add(column);
        keys.add(objectType.key(id));
      }
      Map<byte[], byte[]> results = database.multiGet(columns, keys);
      List<V> values = new ArrayList<>();
      for (byte[] key : keys) {
        values.add(objectType.val(results.get(key)));
      }
      return values;
    } catch (Exception e) {
      throw new DataStoreException(e);
    }
  }

  @Override
  public void delete(K id) throws DataStoreException {
    try {
      database.delete(column, objectType.key(id));
    } catch (Exception e) {
      throw new DataStoreException(e);
    }
  }

  @Override
  public void deleteAll(List<K> ids) throws DataStoreException {
    for (K id : ids) {
      delete(id);
    }
  }

  @Override
  public void close() {

  }

  public static <K, V> RocksdbStore<K, V> open(
      RocksDB database, ColumnFamilyHandle handle, ObjectType<K, V> type) {
    return new RocksdbStore<>(database, handle, type);
  }
}
