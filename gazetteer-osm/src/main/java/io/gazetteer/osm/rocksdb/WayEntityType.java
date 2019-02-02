package io.gazetteer.osm.rocksdb;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.osm.domain.Info;
import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.domain.User;
import io.gazetteer.osm.domain.Way;

public class WayEntityType implements EntityType<Way> {

  @Override
  public byte[] serialize(Way entity) {
    return Rocksdb.Way.newBuilder()
        .setId(entity.getInfo().getId())
        .setVersion(entity.getInfo().getVersion())
        .setUid(entity.getInfo().getUser().getId())
        .setUser(entity.getInfo().getUser().getName())
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
    User user = new User(way.getUid(), way.getUser());
    Info info =
        new Info(
            way.getId(),
            way.getVersion(),
            way.getTimestamp(),
            way.getChangeset(),
            user,
            way.getTagsMap());
    return new Way(info, way.getNodesList());
  }
}
