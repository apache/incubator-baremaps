package io.gazetteer.osm.rocksdb;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.User;
import io.gazetteer.osm.model.Way;

public class WayType implements EntityType<Way> {

  @Override
  public byte[] serialize(Way entity) {
    return Rocksdb.Way.newBuilder()
        .setId(entity.getInfo().getId())
        .setVersion(entity.getInfo().getVersion())
        .setUid(entity.getInfo().getUid())
        .setTimestamp(entity.getInfo().getTimestamp())
        .setChangeset(entity.getInfo().getChangeset())
        .putAllTags(entity.getInfo().getTags())
        .addAllNodes(entity.getNodes())
        .build()
        .toByteArray();
  }

  @Override
  public Way deserialize(byte[] bytes) throws InvalidProtocolBufferException {
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
