package io.gazetteer.osm.rocksdb;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Way;

import java.io.IOException;

public class WayType implements ObjectType<Long, Way> {

  @Override
  public Long ext(Way val) {
    return val.getInfo().getId();
  }

  @Override
  public byte[] key(Long key) {
    return String.format("%019d", key).getBytes();
  }

  @Override
  public Long key(byte[] bytes) {
    return Long.parseLong(new String(bytes));
  }

  @Override
  public byte[] val(Way val) throws IOException {
    return Rocksdb.Way.newBuilder()
            .setId(val.getInfo().getId())
            .setVersion(val.getInfo().getVersion())
            .setUid(val.getInfo().getUserId())
            .setTimestamp(val.getInfo().getTimestamp())
            .setChangeset(val.getInfo().getChangeset())
            .putAllTags(val.getInfo().getTags())
            .addAllNodes(val.getNodes())
            .build()
            .toByteArray();
  }

  @Override
  public Way val(byte[] bytes) throws InvalidProtocolBufferException {
    Rocksdb.Way way = Rocksdb.Way.parseFrom(bytes);
    Info info =
        new Info(
            way.getId(),
            way.getVersion(),
            way.getTimestamp(),
            way.getChangeset(),
            way.getUid(),
            way.getTagsMap());
    return new Way(info, way.getNodesList());
  }
}
