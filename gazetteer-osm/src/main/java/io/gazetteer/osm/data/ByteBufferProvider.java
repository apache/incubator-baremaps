package io.gazetteer.osm.data;

import java.nio.ByteBuffer;

public interface ByteBufferProvider {

  int count();

  int size();

  ByteBuffer get(int index);

}
