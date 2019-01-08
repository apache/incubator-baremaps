package gazetteer.osm.leveldb;

import com.google.protobuf.InvalidProtocolBufferException;
import gazetteer.osm.model.Entity;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.fusesource.leveldbjni.JniDBFactory.*;

public class EntityStore<E extends Entity> {

    private final DB db;

    private final DataType<E> dataType;

    public EntityStore(DB db, DataType<E> dataType) {
        this.db = db;
        this.dataType = dataType;
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

    public List<E> getAll(List<Long> keys) {
        try {

            List<E> values = new ArrayList<>();
            for (Long key : keys) {
                values.add(get(key));
            }
            return values;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] val(E value) throws IOException {
        return dataType.serialize(value);
    }

    private E val(byte[] value) throws InvalidProtocolBufferException {
        return dataType.deserialize(value);
    }

    private byte[] key(long key) {
        return bytes(String.format("%019d", 1));
    }

    private long key(byte[] key) {
        return Long.parseLong(asString(key));
    }

}
