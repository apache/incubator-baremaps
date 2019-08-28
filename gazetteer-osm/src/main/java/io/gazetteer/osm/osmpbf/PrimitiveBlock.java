package io.gazetteer.osm.osmpbf;

import com.google.common.base.Objects;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Member;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PrimitiveBlock {

  private final List<Node> nodes;

  private final List<Way> ways;

  private final List<Relation> relations;

  public PrimitiveBlock(List<Node> nodes, List<Way> ways, List<Relation> relations) {
    checkNotNull(nodes);
    checkNotNull(ways);
    checkNotNull(relations);
    this.nodes = nodes;
    this.ways = ways;
    this.relations = relations;
  }

  public List<Node> getNodes() {
    return nodes;
  }

  public List<Way> getWays() {
    return ways;
  }

  public List<Relation> getRelations() {
    return relations;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PrimitiveBlock that = (PrimitiveBlock) o;
    return Objects.equal(nodes, that.nodes)
        && Objects.equal(ways, that.ways)
        && Objects.equal(relations, that.relations);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(nodes, ways, relations);
  }

  public static PrimitiveBlock parse(Osmformat.PrimitiveBlock primitiveBlock) {
    return new Builder(primitiveBlock).build();
  }

  protected static class Builder {

    private final Osmformat.PrimitiveBlock primitiveBlock;
    private final int granularity;
    private final int dateGranularity;
    private final long latOffset;
    private final long lonOffset;
    private final String[] stringTable;

    public Builder(Osmformat.PrimitiveBlock primitiveBlock) {
      checkNotNull(primitiveBlock);
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

    public PrimitiveBlock build() {
      return new PrimitiveBlock(readDenseNodes(), readWays(), readRelations());
    }

    public List<Node> readDenseNodes() {
      return primitiveBlock.getPrimitivegroupList()
          .stream()
          .flatMap(group -> readDenseNodes(group.getDense()))
          .collect(Collectors.toList());
    }

    private Stream<Node> readDenseNodes(Osmformat.DenseNodes input) {
      List<Node> nodes = new ArrayList<>();
      long id = 0;
      long lat = 0;
      long lon = 0;
      long timestamp = 0;
      long changeset = 0;
      int sid = 0;
      int uid = 0;

      // Index into the keysvals array.
      int j = 0;
      for (int i = 0; i < input.getIdCount(); i++) {
        id = input.getId(i) + id;

        Osmformat.DenseInfo info = input.getDenseinfo();
        int version = info.getVersion(i);
        uid = info.getUid(i) + uid;
        sid = info.getUserSid(i) + sid;
        timestamp = info.getTimestamp(i) + timestamp;
        changeset = info.getChangeset(i) + changeset;
        lat = input.getLat(i) + lat;
        lon = input.getLon(i) + lon;

        // If empty, assume that nothing here has keys or vals.
        Map<String, String> tags = new HashMap<>();
        if (input.getKeysValsCount() > 0) {
          while (input.getKeysVals(j) != 0) {
            int keyid = input.getKeysVals(j++);
            int valid = input.getKeysVals(j++);
            tags.put(getString(keyid), getString(valid));
          }
          j++; // Skip over the '0' delimiter.
        }

        Info data = new Info(id, version, getTimestamp(timestamp), changeset, uid, tags);
        nodes.add(new Node(data, getLon(lon), getLat(lat)));
      }
      return nodes.stream();
    }

    public List<Node> readNodes() {
      return primitiveBlock.getPrimitivegroupList()
          .stream()
          .flatMap(group -> readNodes(group.getNodesList()))
          .collect(Collectors.toList());
    }

    private Stream<Node> readNodes(List<Osmformat.Node> input) {
      List<Node> nodes = new ArrayList<>();
      for (Osmformat.Node e : input) {
        Info info = createEntityData(e.getId(), e.getInfo(), e.getKeysList(), e.getValsList());
        long lon = e.getLon();
        long lat = e.getLat();
        nodes.add(new Node(info, getLon(lon), getLat(lat)));
      }
      return nodes.stream();
    }

    public List<Way> readWays() {
      return primitiveBlock.getPrimitivegroupList()
          .stream()
          .flatMap(group -> readWays(group.getWaysList()))
          .collect(Collectors.toList());
    }

    private Stream<Way> readWays(List<Osmformat.Way> input) {
      List<Way> ways = new ArrayList<>();
      for (Osmformat.Way e : input) {
        Info info = createEntityData(e.getId(), e.getInfo(), e.getKeysList(), e.getValsList());
        long nid = 0;
        List<Long> nodes = new ArrayList<>();
        for (int index = 0; index < e.getRefsCount(); index++) {
          nid = nid + e.getRefs(index);
          nodes.add(nid);
        }
        ways.add(new Way(info, nodes));
      }
      return ways.stream();
    }

    public List<Relation> readRelations() {
      return primitiveBlock.getPrimitivegroupList()
          .stream()
          .flatMap(group -> readRelations(group.getRelationsList()))
          .collect(Collectors.toList());
    }

    protected Stream<Relation> readRelations(List<Osmformat.Relation> input) {
      List<Relation> relations = new ArrayList<>();
      for (Osmformat.Relation r : input) {
        Info info = createEntityData(r.getId(), r.getInfo(), r.getKeysList(), r.getValsList());
        long mid = 0;
        List<Member> members = new ArrayList<>();
        for (int j = 0; j < r.getMemidsCount(); j++) {
          mid = mid + r.getMemids(j);
          String role = getString(r.getRolesSid(j));
          Member.Type type = null;
          if (r.getTypes(j) == Osmformat.Relation.MemberType.NODE) {
            type = Member.Type.node;
          } else if (r.getTypes(j) == Osmformat.Relation.MemberType.WAY) {
            type = Member.Type.way;
          } else if (r.getTypes(j) == Osmformat.Relation.MemberType.RELATION) {
            type = Member.Type.relation;
          } else {
            throw new IllegalArgumentException("Unsupported MemberType");
          }
          members.add(new Member(mid, type, role));
        }
        relations.add(new Relation(info, members));
      }
      return relations.stream();
    }

    protected Info createEntityData(
        long id, Osmformat.Info info, List<Integer> keys, List<Integer> vals) {
      LocalDateTime timestamp = getTimestamp(info.getTimestamp());
      int version = info.getVersion();
      long changeset = info.getChangeset();
      int uid = info.getUid();

      Map<String, String> tags = new HashMap<>();
      for (int t = 0; t < keys.size(); t++) {
        tags.put(getString(keys.get(t)), getString(vals.get(t)));
      }

      return new Info(id, version, timestamp, changeset, uid, tags);
    }

    protected String getString(int id) {
      return stringTable[id];
    }

    protected double getLat(long lat) {
      return (granularity * lat + latOffset) * .000000001;
    }

    protected double getLon(long lon) {
      return (granularity * lon + lonOffset) * .000000001;
    }

    protected LocalDateTime getTimestamp(long timestamp) {
      return LocalDateTime.ofInstant(Instant.ofEpochMilli(dateGranularity * timestamp), TimeZone.getDefault().toZoneId());
    }

  }
}
