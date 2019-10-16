package io.gazetteer.osm.data;

import java.nio.ByteBuffer;

public class MappedByteBufferProvider implements ByteBufferProvider {

  public MappedByteBufferProvider() {

  }

  @Override
  public int count() {
    return 0;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public ByteBuffer get(int index) {
    return null;
  }
}
