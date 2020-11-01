package com.baremaps.osm.pbf;

import com.baremaps.osm.binary.Osmformat;
import com.baremaps.osm.binary.Osmformat.DenseNodes;
import com.baremaps.osm.binary.Osmformat.PrimitiveBlock;
import com.baremaps.osm.binary.Osmformat.PrimitiveGroup;
import com.baremaps.osm.domain.Info;
import com.baremaps.osm.domain.Member;
import com.baremaps.osm.domain.Member.MemberType;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

public class DataBlockReader {

  private final Osmformat.PrimitiveBlock primitiveBlock;
  private final int granularity;
  private final int dateGranularity;
  private final long latOffset;
  private final long lonOffset;
  private final String[] stringTable;

  public DataBlockReader(PrimitiveBlock primitiveBlock) {
    this.primitiveBlock = primitiveBlock;
    this.granularity = primitiveBlock.getGranularity();
    this.latOffset = primitiveBlock.getLatOffset();
    this.lonOffset = primitiveBlock.getLonOffset();
    this.dateGranularity = primitiveBlock.getDateGranularity();
    this.stringTable = new String[primitiveBlock.getStringtable().getSCount()];
    for (int i = 0; i < stringTable.length; i++) {
      stringTable[i] = primitiveBlock.getStringtable().getS(i).toStringUtf8();
    }
  }

  public Stream<Node> readDenseNodes() {
    Stream.Builder<Node> builder = Stream.builder();
    for (PrimitiveGroup group : primitiveBlock.getPrimitivegroupList()) {
      DenseNodes denseNodes = group.getDense();

      long id = 0;
      long lat = 0;
      long lon = 0;
      long timestamp = 0;
      long changeset = 0;
      int sid = 0;
      int uid = 0;

      // Index into the keysvals array.
      int j = 0;
      for (int i = 0; i < denseNodes.getIdCount(); i++) {
        id = denseNodes.getId(i) + id;

        Osmformat.DenseInfo denseInfo = denseNodes.getDenseinfo();
        int version = denseInfo.getVersion(i);
        uid = denseInfo.getUid(i) + uid;
        sid = denseInfo.getUserSid(i) + sid;
        timestamp = denseInfo.getTimestamp(i) + timestamp;
        changeset = denseInfo.getChangeset(i) + changeset;
        lat = denseNodes.getLat(i) + lat;
        lon = denseNodes.getLon(i) + lon;

        // If empty, assume that nothing here has keys or vals.
        Map<String, String> tags = new HashMap<>();
        if (denseNodes.getKeysValsCount() > 0) {
          while (denseNodes.getKeysVals(j) != 0) {
            int keyid = denseNodes.getKeysVals(j++);
            int valid = denseNodes.getKeysVals(j++);
            tags.put(getString(keyid), getString(valid));
          }
          j++; // Skip over the '0' delimiter.
        }

        Info info = new Info(version, getTimestamp(timestamp), changeset, uid);
        builder.accept(new Node(id, info, tags, getLon(lon), getLat(lat)));
      }
    }
    return builder.build();
  }

  public Stream<Node> readNodes() {
    Stream.Builder<Node> builder = Stream.builder();
    for (PrimitiveGroup group : primitiveBlock.getPrimitivegroupList()) {
      for (Osmformat.Node node : group.getNodesList()) {
        long id = node.getId();
        int version = node.getInfo().getVersion();
        LocalDateTime timestamp = getTimestamp(node.getInfo().getTimestamp());
        long changeset = node.getInfo().getChangeset();
        int uid = node.getInfo().getUid();
        Map<String, String> tags = new HashMap<>();
        for (int t = 0; t < node.getKeysList().size(); t++) {
          tags.put(getString(node.getKeysList().get(t)), getString(node.getKeysList().get(t)));
        }
        double lon = getLon(node.getLon());
        double lat = getLat(node.getLat());

        Info info = new Info(version, timestamp, changeset, uid);
        builder.accept(new Node(id, info, tags, lon, lat));
      }
    }
    return builder.build();
  }

  public Stream<Way> readWays() {
    Stream.Builder<Way> builder = Stream.builder();
    for (PrimitiveGroup group : primitiveBlock.getPrimitivegroupList()) {
      for (Osmformat.Way way : group.getWaysList()) {
        long id = way.getId();
        int version = way.getInfo().getVersion();
        LocalDateTime timestamp = getTimestamp(way.getInfo().getTimestamp());
        long changeset = way.getInfo().getChangeset();
        int uid = way.getInfo().getUid();
        Map<String, String> tags = getTags(way.getKeysList(), way.getValsList());
        long nid = 0;
        List<Long> nodes = new ArrayList<>();
        for (int index = 0; index < way.getRefsCount(); index++) {
          nid = nid + way.getRefs(index);
          nodes.add(nid);
        }

        Info info = new Info(version, timestamp, changeset, uid);
        builder.accept(new Way(id, info, tags, nodes));
      }
    }
    return builder.build();
  }

  public Stream<Relation> readRelations() {
    Stream.Builder<Relation> builder = Stream.builder();
    for (PrimitiveGroup group : primitiveBlock.getPrimitivegroupList()) {
      for (Osmformat.Relation relation : group.getRelationsList()) {
        long id = relation.getId();
        int version = relation.getInfo().getVersion();
        LocalDateTime timestamp = getTimestamp(relation.getInfo().getTimestamp());
        long changeset = relation.getInfo().getChangeset();
        int uid = relation.getInfo().getUid();
        Map<String, String> tags = getTags(relation.getKeysList(), relation.getValsList());

        long mid = 0;
        List<Member> members = new ArrayList<>();
        for (int j = 0; j < relation.getMemidsCount(); j++) {
          mid = mid + relation.getMemids(j);
          String role = getString(relation.getRolesSid(j));
          MemberType type = type(relation.getTypes(j));
          members.add(new Member(mid, type, role));
        }

        Info info = new Info(version, timestamp, changeset, uid);
        builder.accept(new Relation(id, info, tags, members));
      }
    }
    return builder.build();
  }

  protected MemberType type(Osmformat.Relation.MemberType type) {
    switch (type) {
      case NODE:
        return MemberType.node;
      case WAY:
        return MemberType.way;
      case RELATION:
        return MemberType.relation;
      default:
        throw new UnsupportedOperationException();
    }
  }

  protected double getLat(long lat) {
    return (granularity * lat + latOffset) * .000000001;
  }

  protected double getLon(long lon) {
    return (granularity * lon + lonOffset) * .000000001;
  }

  protected LocalDateTime getTimestamp(long timestamp) {
    return LocalDateTime
        .ofInstant(Instant.ofEpochMilli(dateGranularity * timestamp), TimeZone.getDefault().toZoneId());
  }

  protected Map<String, String> getTags(List<Integer> keys, List<Integer> vals) {
    Map<String, String> tags = new HashMap<>();
    for (int t = 0; t < keys.size(); t++) {
      tags.put(getString(keys.get(t)), getString(vals.get(t)));
    }
    return tags;
  }

  protected String getString(int id) {
    return stringTable[id];
  }

}
