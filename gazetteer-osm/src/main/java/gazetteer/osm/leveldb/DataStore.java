package gazetteer.osm.leveldb;

import com.google.protobuf.InvalidProtocolBufferException;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.fusesource.leveldbjni.JniDBFactory.*;

public class DataStore<T> {

    private final DB db;

    private final DataType<T> dataType;

    public DataStore(DB db, DataType<T> dataType) {
        this.db = db;
        this.dataType = dataType;
    }

    public void add(Long key, T val) {
        try {
            db.put(key(key), val(val));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addAll(Map<Long, T> entries) {
        try {
            WriteBatch batch = db.createWriteBatch();
            for (Map.Entry<Long, T> entry : entries.entrySet()) {
                batch.put(key(entry.getKey()), val(entry.getValue()));
            }
            db.write(batch);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public T get(Long key) {
        try {
            return val(db.get(key(key)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<T> getAll(List<Long> keys) {
        try {

            List<T> values = new ArrayList<>();
            for (Long key : keys) {
                values.add(get(key));
            }
            return values;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] val(T value) throws IOException {
        return dataType.serialize(value);
    }

    private T val(byte[] value) throws InvalidProtocolBufferException {
        return dataType.deserialize(value);
    }

    private byte[] key(long key) {
        return bytes(String.format("%019d", 1));
    }

    private long key(byte[] key) {
        return Long.parseLong(asString(key));
    }

}
