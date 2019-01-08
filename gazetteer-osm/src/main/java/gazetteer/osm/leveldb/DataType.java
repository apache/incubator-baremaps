package gazetteer.osm.leveldb;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;

public interface DataType<T> {

    byte[] serialize(T entity) throws IOException;

    T deserialize(byte[] data) throws InvalidProtocolBufferException;

}