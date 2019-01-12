package gazetteer.osm.cache;

import com.google.protobuf.InvalidProtocolBufferException;
import gazetteer.osm.model.Entity;
import org.rocksdb.RocksDB;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EntityCache<E extends Entity> {

    private final RocksDB db;

    private final EntityType<E> entityType;

    public EntityCache(RocksDB db, EntityType<E> entityType) {
        this.db = db;
        this.entityType = entityType;
    }

    public void add(E entity) {
        try {
            db.put(key(entity.getId()), val(entity));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addAll(Collection<E> entities) {
        try (WriteBatch batch = new WriteBatch()) {
            for (E entity : entities) {
                batch.put(key(entity.getId()), val(entity));
            }
            db.write(new WriteOptions(), batch);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public E get(Long id) {
        try {
            return val(db.get(key(id)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<E> getAll(List<Long> ids) {
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
            throw new RuntimeException(e);
        }
    }

    public void delete(Long id) {
        try {
            db.delete(key(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAll(List<Long> ids) {
        try {
            List<E> values = new ArrayList<>();
            for (Long id : ids) {
                db.delete(key(id));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

}
