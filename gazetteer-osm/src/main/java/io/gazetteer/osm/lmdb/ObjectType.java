package io.gazetteer.osm.lmdb;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ObjectType<K, V> {

  K ext(V val);

  ByteBuffer key(K key);

  K key(ByteBuffer bytes);

  ByteBuffer val(V val) throws IOException;

  V val(ByteBuffer data) throws InvalidProtocolBufferException;
}
