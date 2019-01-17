package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.domain.*;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A adaptation of the OsmosisBinaryParser
 */
public class PrimitiveBlockReader {

    private final Osmformat.PrimitiveBlock block;
    private final int granularity;
    private final int dateGranularity;
    private final long latOffset;
    private final long lonOffset;
    private final String[] stringTable;

    public PrimitiveBlockReader(Osmformat.PrimitiveBlock block) {
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

    public PrimitiveBlock read() {
        List<Node> nodes = new ArrayList<>();
        List<Way> ways = new ArrayList<>();
        List<Relation> relations = new ArrayList<>();
        for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
            readDenseNodes(group.getDense(), nodes);
            readWays(group.getWaysList(), ways);
            readRelations(group.getRelationsList(), relations);
        }
        return new PrimitiveBlock(nodes, ways, relations);
    }

    public List<Node> readDenseNodes() {
        List<Node> nodes = new ArrayList<>();
        for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
            readDenseNodes(group.getDense(), nodes);
        }
        return nodes;
    }

    private void readDenseNodes(Osmformat.DenseNodes input, List<Node> output) {
        long id = 0;
        long lat = 0;
        long lon = 0;
        long timestamp = 0;
        long changeset = 0;
        int sid = 0;
        int uid = 0;
        int j = 0; // Index into the keysvals array.

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

            Info data = new Info(id, version, getTimestamp(timestamp), changeset, new User(uid, getString(sid)), tags);
            output.add(new Node(data, getLon(lon), getLat(lat)));
        }
    }

    public List<Node> readNodes() {
        List<Node> nodes = new ArrayList<>();
        for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
            readNodes(group.getNodesList(), nodes);
        }
        return nodes;
    }

    private void readNodes(List<Osmformat.Node> input, List<Node> output) {
        for (Osmformat.Node e : input) {
            Info info = createEntityData(e.getId(), e.getInfo(), e.getKeysList(), e.getValsList());
            long lon = e.getLon();
            long lat = e.getLat();
            output.add(new Node(info, getLon(lon), getLat(lat)));
        }
    }

    public List<Way> readWays() {
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

    public List<Relation> readRelations() {
        List<Relation> relations = new ArrayList<>();
        for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
            readRelations(group.getRelationsList(), relations);
        }
        return relations;
    }

    private void readRelations(List<Osmformat.Relation> input, List<Relation> output) {
        for (Osmformat.Relation e : input) {
            Info info = createEntityData(e.getId(), e.getInfo(), e.getKeysList(), e.getValsList());
            long mid = 0;
            List<Member> members = new ArrayList<>();
            for (int j = 0; j < e.getMemidsCount(); j++) {
                mid = mid + e.getMemids(j);
                String role = getString(e.getRolesSid(j));
                MemberType etype = null;
                if (e.getTypes(j) == Osmformat.Relation.MemberType.NODE) {
                    etype = MemberType.Node;
                } else if (e.getTypes(j) == Osmformat.Relation.MemberType.WAY) {
                    etype = MemberType.Way;
                } else if (e.getTypes(j) == Osmformat.Relation.MemberType.RELATION) {
                    etype = MemberType.Relation;
                } else {
                    assert false; // TODO: throw an exception (invalid argument?)
                }
                members.add(new Member(mid, etype, role));
            }
            output.add(new Relation(info, members));
        }
    }

    private Info createEntityData(long id, Osmformat.Info info, List<Integer> keys, List<Integer> vals) {
        long timestamp = getTimestamp(info.getTimestamp());
        int version = info.getVersion();
        long changeset = info.getChangeset();

        int uid = info.getUid();
        String name = getString(info.getUserSid());
        User user = new User(uid, name);

        Map<String, String> tags = new HashMap<>();
        for (int t = 0; t < keys.size(); t++) {
            tags.put(getString(keys.get(t)), getString(vals.get(t)));
        }

        return new Info(id, version, timestamp, changeset, user, tags);
    }


    private String getString(int id) {
        return stringTable[id];
    }

    private double getLat(long lat) {
        return (granularity * lat + latOffset) * .000000001;
    }

    private double getLon(long lon) {
        return (granularity * lon + lonOffset) * .000000001;
    }

    private long getTimestamp(long timestamp) {
        return dateGranularity * timestamp;
    }

}
