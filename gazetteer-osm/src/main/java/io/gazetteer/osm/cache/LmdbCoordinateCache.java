package io.gazetteer.osm.cache;

import java.nio.ByteBuffer;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.locationtech.jts.geom.Coordinate;

public class LmdbCoordinateCache extends LmdbCache<Long, Coordinate> {

  public LmdbCoordinateCache(Env<ByteBuffer> env, Dbi<ByteBuffer> database) {
    super(env, database);
  }

  @Override
  public ByteBuffer buffer(Long key) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(20);
    buffer.put(String.format("%020d", key).getBytes()).flip();
    return buffer.putLong(key);
  }

  @Override
  public Coordinate read(ByteBuffer buffer) {
    if (buffer == null || buffer.get() == 0) {
      return null;
    }
    double lon = buffer.getDouble();
    double lat = buffer.getDouble();
    return new Coordinate(lon, lat);
  }

  @Override
  public ByteBuffer write(Coordinate value) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(17);
    if (value != null) {
      buffer.put((byte) 1);
      buffer.putDouble(value.getX());
      buffer.putDouble(value.getY());
    }
    buffer.flip();
    return buffer;
  }

}
