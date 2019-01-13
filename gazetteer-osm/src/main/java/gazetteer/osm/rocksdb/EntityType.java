package gazetteer.osm.rocksdb;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;

public interface EntityType<T> {

    byte[] serialize(T entity) throws IOException;

    T deserialize(byte[] data) throws InvalidProtocolBufferException;

}