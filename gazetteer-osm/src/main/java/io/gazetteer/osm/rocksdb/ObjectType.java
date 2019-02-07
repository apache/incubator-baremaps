package io.gazetteer.osm.rocksdb;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;

public interface ObjectType<K, V> {

  K ext(V val);

  byte[] key(K key);

  K key(byte[] bytes);

  byte[] val(V val) throws IOException;

  V val(byte[] data) throws InvalidProtocolBufferException;
}
