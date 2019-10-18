package io.gazetteer.osm.cache;

import java.nio.ByteBuffer;

public interface CacheMapper<T> {

  T read(ByteBuffer bytes);

  ByteBuffer write(T value);

}
