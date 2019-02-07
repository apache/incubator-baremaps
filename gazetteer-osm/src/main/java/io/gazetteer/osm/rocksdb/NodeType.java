package io.gazetteer.osm.rocksdb;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;

public class NodeType implements ObjectType<Long, Node> {

  @Override
  public Long ext(Node val) {
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
  public byte[] val(Node val) {
    return Rocksdb.Node.newBuilder()
        .setId(val.getInfo().getId())
        .setVersion(val.getInfo().getVersion())
        .setUid(val.getInfo().getUserId())
        .setTimestamp(val.getInfo().getTimestamp())
        .setChangeset(val.getInfo().getChangeset())
        .setLon(val.getLon())
        .setLat(val.getLat())
        .putAllTags(val.getInfo().getTags())
        .build()
        .toByteArray();
  }

  @Override
  public Node val(byte[] bytes) throws InvalidProtocolBufferException {
    Rocksdb.Node node = Rocksdb.Node.parseFrom(bytes);
    Info info =
        new Info(
            node.getId(),
            node.getVersion(),
            node.getTimestamp(),
            node.getChangeset(),
            node.getUid(),
            node.getTagsMap());
    return new Node(info, node.getLon(), node.getLat());
  }
}
