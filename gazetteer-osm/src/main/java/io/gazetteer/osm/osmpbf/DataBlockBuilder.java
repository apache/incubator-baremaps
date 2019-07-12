package io.gazetteer.osm.osmpbf;

import static com.google.common.base.Preconditions.checkNotNull;

import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Member;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

public class DataBlockBuilder {

  private final Osmformat.PrimitiveBlock block;
  private final int granularity;
  private final int dateGranularity;
  private final long latOffset;
  private final long lonOffset;
  private final String[] stringTable;

  public DataBlockBuilder(Osmformat.PrimitiveBlock block) {
    checkNotNull(block);
    this.block = block;
    this.granularity = block.getGranularity();
    this.latOffset = block.getLatOffset();
    this.lonOffset = block.getLonOffset();
    this.dateGranularity = block.getDateGranularity();
    this.stringTable = new String[block.getStringtable().getSCount()];
    for (int i = 0; i < stringTable.length; i++) {
      stringTable[i] = block.getStringtable().getS(i).toStringUtf8();
    }
  }

  public DataBlock build() {
    List<Node> nodes = new ArrayList<>();
    List<Way> ways = new ArrayList<>();
    List<Relation> relations = new ArrayList<>();
    for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
      readDenseNodes(group.getDense(), nodes);
      readWays(group.getWaysList(), ways);
      readRelations(group.getRelationsList(), relations);
    }
    return new DataBlock(nodes, ways, relations);
  }

  protected List<Node> readDenseNodes() {
    List<Node> nodes = new ArrayList<>();
    for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
      readDenseNodes(group.getDense(), nodes);
    }
    return nodes;
  }

  protected void readDenseNodes(Osmformat.DenseNodes input, List<Node> output) {
    long id = 0;
    long lat = 0;
    long lon = 0;
    long timestamp = 0;
    long changeset = 0;
    int sid = 0;
    int uid = 0;

    // Index into the keysvals array.
    int j = 0;
    for (int i   = 0; i < input.getIdCount(); i++) {
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
      output.add(new Node(data, getLon(lon), getLat(lat)));
    }
  }

  protected List<Node> readNodes() {
    List<Node> nodes = new ArrayList<>();
    for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
      readNodes(group.getNodesList(), nodes);
    }
    return nodes;
  }

  protected void readNodes(List<Osmformat.Node> input, List<Node> output) {
    for (Osmformat.Node e : input) {
      Info info = createEntityData(e.getId(), e.getInfo(), e.getKeysList(), e.getValsList());
      long lon = e.getLon();
      long lat = e.getLat();
      output.add(new Node(info, getLon(lon), getLat(lat)));
    }
  }

  protected List<Way> readWays() {
    List<Way> ways = new ArrayList<>();
    for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
      readWays(group.getWaysList(), ways);
    }
    return ways;
  }

  private void readWays(List<Osmformat.Way> input, List<Way> output) {
    for (Osmformat.Way e : input) {
      Info info = createEntityData(e.getId(), e.getInfo(), e.getKeysList(), e.getValsList());
      long nid = 0;
      List<Long> nodes = new ArrayList<>();
      for (int index = 0; index < e.getRefsCount(); index++) {
        nid = nid + e.getRefs(index);
        nodes.add(nid);
      }
      output.add(new Way(info, nodes));
    }
  }

  protected List<Relation> readRelations() {
    List<Relation> relations = new ArrayList<>();
    for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
      readRelations(group.getRelationsList(), relations);
    }
    return relations;
  }

  protected void readRelations(List<Osmformat.Relation> input, List<Relation> output) {
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
      output.add(new Relation(info, members));
    }
  }

  protected Info createEntityData(
      long id, Osmformat.Info info, List<Integer> keys, List<Integer> vals) {
    long timestamp = getTimestamp(info.getTimestamp());
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

  protected long getTimestamp(long timestamp) {
    return dateGranularity * timestamp;
  }

}
