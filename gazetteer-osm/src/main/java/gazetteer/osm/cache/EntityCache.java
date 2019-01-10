package gazetteer.osm.cache;

import com.google.protobuf.InvalidProtocolBufferException;
import gazetteer.osm.model.Entity;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.fusesource.leveldbjni.JniDBFactory.*;

public class EntityCache<E extends Entity> {

    private final DB db;

    private final EntityType<E> entityType;

    public EntityCache(DB db, EntityType<E> entityType) {
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
        try {
            WriteBatch batch = db.createWriteBatch();
            for (E entity : entities) {
                batch.put(key(entity.getId()), val(entity));
            }
            db.write(batch);
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
            List<E> values = new ArrayList<>();
            for (Long id : ids) {
                values.add(get(id));
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
        return bytes(String.format("%019d", 1));
    }

    private long key(byte[] id) {
        return Long.parseLong(asString(id));
    }

}
