package io.gazetteer.osm.lmdb;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Way;

import java.nio.ByteBuffer;

public class WayType implements ObjectType<Long, Way> {

  @Override
  public Long ext(Way val) {
    return val.getInfo().getId();
  }

  @Override
  public ByteBuffer key(Long key) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(19);
    buffer.put(String.format("%019d", key).getBytes()).flip();
    return buffer;
  }

  @Override
  public Long key(ByteBuffer bytes) {
    return Long.parseLong(new String(bytes.array()));
  }

  @Override
  public ByteBuffer val(Way val) {
    Lmdb.Info info = InfoUtil.info(val.getInfo());
    Lmdb.Way way = Lmdb.Way.newBuilder().setInfo(info).addAllNodes(val.getNodes()).build();
    ByteBuffer buffer = ByteBuffer.allocateDirect(way.getSerializedSize());
    buffer.put(way.toByteString().asReadOnlyByteBuffer()).flip();
    return buffer;
  }

  @Override
  public Way val(ByteBuffer bytes) throws InvalidProtocolBufferException {
    Lmdb.Way way = Lmdb.Way.parseFrom(bytes);
    Info info = InfoUtil.info(way.getInfo());
    return new Way(info, way.getNodesList());
  }
}
