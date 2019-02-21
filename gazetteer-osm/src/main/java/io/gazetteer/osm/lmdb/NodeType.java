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
    Lmdb.Info info = InfoUtil.info(val.getInfo());
    Lmdb.Node node =
        Lmdb.Node.newBuilder().setInfo(info).setLon(val.getLon()).setLat(val.getLat()).build();
    ByteBuffer buffer = ByteBuffer.allocateDirect(node.getSerializedSize());
    buffer.put(node.toByteString().asReadOnlyByteBuffer()).flip();
    return buffer;
  }

  @Override
  public Node val(ByteBuffer bytes) throws InvalidProtocolBufferException {
    Lmdb.Node node = Lmdb.Node.parseFrom(bytes);
    Info info = InfoUtil.info(node.getInfo());
    return new Node(info, node.getLon(), node.getLat());
  }
}
