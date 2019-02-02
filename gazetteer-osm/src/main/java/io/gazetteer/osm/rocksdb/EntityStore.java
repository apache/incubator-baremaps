package io.gazetteer.osm.rocksdb;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.osm.domain.Entity;
import org.rocksdb.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class EntityStore<E extends Entity> implements AutoCloseable {

  static {
    RocksDB.loadLibrary();
  }

  private final RocksDB database;

  private final ColumnFamilyHandle column;

  private final EntityType<E> entityType;

  private EntityStore(RocksDB database, ColumnFamilyHandle column, EntityType<E> entityType) {
    checkNotNull(database);
    checkNotNull(entityType);
    this.database = database;
    this.column = column;
    this.entityType = entityType;
  }

  public void add(E entity) throws EntityStoreException {
    try {
      database.put(column, key(entity.getInfo().getId()), val(entity));
    } catch (Exception e) {
      throw new EntityStoreException(e);
    }
  }

  public void addAll(Collection<E> entities) throws EntityStoreException {
    if (entities.size() > 0) {
      try (WriteBatch batch = new WriteBatch()) {
        for (E entity : entities) {
          batch.put(column, key(entity.getInfo().getId()), val(entity));
        }
        database.write(new WriteOptions(), batch);
      } catch (Exception e) {
        throw new EntityStoreException(e);
      }
    }
  }

  public E get(long id) throws EntityStoreException {
    try {
      return val(database.get(column, key(id)));
    } catch (Exception e) {
      throw new EntityStoreException(e);
    }
  }

  public List<E> getAll(List<Long> ids) throws EntityStoreException {
    try {
      List<ColumnFamilyHandle> columns = new ArrayList<>();
      List<byte[]> keys = new ArrayList<>();
      for (long id : ids) {
        columns.add(column);
        keys.add(key(id));
      }
      Map<byte[], byte[]> results = database.multiGet(columns, keys);
      List<E> values = new ArrayList<>();
      for (byte[] key : keys) {
        values.add(val(results.get(key)));
      }
      return values;
    } catch (Exception e) {
      throw new EntityStoreException(e);
    }
  }

  public void delete(long id) throws EntityStoreException {
    try {
      database.delete(column, key(id));
    } catch (Exception e) {
      throw new EntityStoreException(e);
    }
  }

  public void deleteAll(List<Long> ids) throws EntityStoreException {
    for (Long id : ids) {
      delete(id);
    }
  }

  public void close() {
    database.close();
  }

  private byte[] val(E value) throws IOException {
    return entityType.serialize(value);
  }

  private E val(byte[] value) throws InvalidProtocolBufferException {
    return entityType.deserialize(value);
  }

  private byte[] key(long id) {
    return String.format("%019d", id).getBytes();
  }

  private long key(byte[] id) {
    return Long.parseLong(new String(id));
  }

  public static <E extends Entity> EntityStore<E> open(RocksDB database, String column, EntityType<E> type)
      throws EntityStoreException {
    try {
      final ColumnFamilyHandle handle = database.createColumnFamily(new ColumnFamilyDescriptor(column.getBytes()));
      return new EntityStore<>(database, handle, type);
    } catch (RocksDBException e) {
      throw new EntityStoreException(e);
    }
  }
}
