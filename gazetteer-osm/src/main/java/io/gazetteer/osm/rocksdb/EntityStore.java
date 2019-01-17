package io.gazetteer.osm.rocksdb;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.osm.domain.Entity;
import org.rocksdb.*;

import java.io.File;
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

    private final RocksDB db;

    private final EntityType<E> entityType;

    public EntityStore(RocksDB db, EntityType<E> entityType) {
        checkNotNull(db);
        checkNotNull(entityType);
        this.db = db;
        this.entityType = entityType;
    }

    public void add(E entity) throws EntityStoreException {
        try {
            db.put(key(entity.getInfo().getId()), val(entity));
        } catch (Exception e) {
            throw new EntityStoreException(e);
        }
    }

    public void addAll(Collection<E> entities) throws EntityStoreException {
        try (WriteBatch batch = new WriteBatch()) {
            for (E entity : entities) {
                batch.put(key(entity.getInfo().getId()), val(entity));
            }
            db.write(new WriteOptions(), batch);
        } catch (Exception e) {
            throw new EntityStoreException(e);
        }
    }

    public E get(Long id) throws EntityStoreException {
        try {
            return val(db.get(key(id)));
        } catch (Exception e) {
            throw new EntityStoreException(e);
        }
    }

    public List<E> getAll(List<Long> ids) throws EntityStoreException {
        try {
            List<byte[]> keys = new ArrayList<>();
            for (long id : ids) {
                keys.add(key(id));
            }
            Map<byte[], byte[]> results = db.multiGet(keys);
            List<E> values = new ArrayList<>();
            for (byte[] key : keys) {
                values.add(val(results.get(key)));
            }
            return values;
        } catch (Exception e) {
            throw new EntityStoreException(e);
        }
    }

    public void delete(Long id) throws EntityStoreException {
        try {
            db.delete(key(id));
        } catch (Exception e) {
            throw new EntityStoreException(e);
        }
    }

    public void deleteAll(List<Long> ids) throws EntityStoreException {
        try {
            List<E> values = new ArrayList<>();
            for (Long id : ids) {
                db.delete(key(id));
            }
        } catch (Exception e) {
            throw new EntityStoreException(e);
        }
    }

    public void close() {
        db.close();
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

    public static <E extends Entity> EntityStore<E> open(File database, EntityType<E> type) throws EntityStoreException {
        try {

            final Options options = new Options()
                    .setCreateIfMissing(true)
                    .setCompressionType(CompressionType.NO_COMPRESSION);
            final RocksDB db = RocksDB.open(options, database.getPath());
            return new EntityStore<>(db, type);
        } catch (RocksDBException e) {
            throw new EntityStoreException(e);
        }
    }

}
