package io.gazetteer.osm.lmdb;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Member;
import io.gazetteer.osm.model.Relation;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

public class RelationType implements ObjectType<Long, Relation> {

  @Override
  public Long ext(Relation val) {
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
  public ByteBuffer val(Relation val) {
    List<Lmdb.Member> members =
        val.getMembers().stream()
            .map(
                m ->
                    Lmdb.Member.newBuilder()
                        .setRef(m.getRef())
                        .setType(m.getType().name())
                        .setRole(m.getRole())
                        .build())
            .collect(Collectors.toList());
    Lmdb.Relation relation =
        Lmdb.Relation.newBuilder()
            .setInfo(InfoUtil.info(val.getInfo()))
            .addAllMembers(members)
            .build();
    ByteBuffer buffer = ByteBuffer.allocateDirect(relation.getSerializedSize());
    buffer.put(relation.toByteString().asReadOnlyByteBuffer()).flip();
    return buffer;
  }

  @Override
  public Relation val(ByteBuffer bytes) throws InvalidProtocolBufferException {
    Lmdb.Relation relation = Lmdb.Relation.parseFrom(bytes);
    Info info = InfoUtil.info(relation.getInfo());
    List<Member> members =
        relation.getMembersList().stream()
            .map(m -> new Member(m.getRef(), Member.Type.valueOf(m.getType()), m.getRole()))
            .collect(Collectors.toList());
    return new Relation(info, members);
  }
}
