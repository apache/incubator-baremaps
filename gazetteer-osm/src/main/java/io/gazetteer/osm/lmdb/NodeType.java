package io.gazetteer.osm.lmdb;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;

import java.nio.ByteBuffer;

public class NodeType implements ObjectType<Long, Node> {

  @Override
  public Long ext(Node val) {
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
  public ByteBuffer val(Node val) {
    Rocksdb.Node node = Rocksdb.Node.newBuilder()
            .setId(val.getInfo().getId())
            .setVersion(val.getInfo().getVersion())
            .setUid(val.getInfo().getUserId())
            .setTimestamp(val.getInfo().getTimestamp())
            .setChangeset(val.getInfo().getChangeset())
            .setLon(val.getLon())
            .setLat(val.getLat())
            .putAllTags(val.getInfo().getTags())
            .build();
    ByteBuffer buffer = ByteBuffer.allocateDirect(node.getSerializedSize());
    buffer.put(node.toByteString().asReadOnlyByteBuffer()).flip();
    return buffer;
  }

  @Override
  public Node val(ByteBuffer bytes) throws InvalidProtocolBufferException {
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
