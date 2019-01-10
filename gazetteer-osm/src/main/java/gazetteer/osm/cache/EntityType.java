package gazetteer.osm.cache;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;

public interface EntityType<T> {

    byte[] serialize(T entity) throws IOException;

    T deserialize(byte[] data) throws InvalidProtocolBufferException;

}