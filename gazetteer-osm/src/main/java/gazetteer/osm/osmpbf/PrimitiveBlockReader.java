package gazetteer.osm.osmpbf;

import gazetteer.osm.model.*;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.util.ArrayList;
import java.util.List;

/**
 * A adaptation of the OsmosisBinaryParser
 */
public class PrimitiveBlockParser {

    private Osmformat.PrimitiveBlock block;

    private int granularity;
    private int dateGranularity;
    private long latOffset;
    private long lonOffset;
    private String stringTable[];

    public PrimitiveBlockParser(Osmformat.PrimitiveBlock block) {
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

    public PrimitiveBlock parse() {
        return new PrimitiveBlock(nodes, ways, relations);
    }



    public List<Node> getDenseNodes() {
        ArrayList<Node> nodes = new ArrayList<>();
        for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
            Osmformat.DenseNodes osmNodes = group.getDense();
            nodes.addAll(getDenseNodes(osmNodes));
        }
        return nodes;
    }
    
    private List<Node> getDenseNodes(Osmformat.DenseNodes osmNodes) {
        ArrayList<Node> nodes = new ArrayList<>();

        long id = 0;
        long lat = 0;
        long lon = 0;
        long timestamp = 0;
        long changeset = 0;
        int userSid = 0;
        int uid = 0;
        int j = 0; // Index into the keysvals array.

        for (int i = 0; i < osmNodes.getIdCount(); i++) {
            id = osmNodes.getId(i) + id;

            Osmformat.DenseInfo info = osmNodes.getDenseinfo();
            int version = info.getVersion(i);
            uid = info.getUid(i) + uid;
            userSid = info.getUserSid(i) + userSid;
            timestamp = info.getTimestamp(i) + timestamp;
            changeset = info.getChangeset(i) + changeset;
            lat = osmNodes.getLat(i) + lat;
            lon = osmNodes.getLon(i) + lon;

            // If empty, assume that nothing here has keys or vals.
            List<String> keys = new ArrayList<>(0);
            List<String> vals = new ArrayList<>(0);
            if (osmNodes.getKeysValsCount() > 0) {
                while (osmNodes.getKeysVals(j) != 0) {
                    int keyid = osmNodes.getKeysVals(j++);
                    int valid = osmNodes.getKeysVals(j++);
                    keys.add(getString(keyid));
                    vals.add(getString(valid));
                }
                j++; // Skip over the '0' delimiter.
            }

            nodes.add(new Node(id, version, uid, getString(userSid), getTimestamp(timestamp), changeset, getLon(lon), getLat(lat), keys, vals));
        }

        return nodes;
    }

    public List<Way> getWays() {
        List<Way> ways = new ArrayList<>();
        for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
            List<Osmformat.Way> osmWays = group.getWaysList();
            ways.addAll(getWays(osmWays));
        }
        return ways;
    }

    private List<Way> getWays(List<Osmformat.Way> osmWays) {
        List<Way> ways = new ArrayList<>();
        for (Osmformat.Way w : osmWays) {
            long id = w.getId();

            Osmformat.Info info = w.getInfo();
            long timestamp = getTimestamp(info.getTimestamp());
            int version = info.getVersion();
            int uid = info.getUid();
            String user = getString(info.getUserSid());
            long changeset = info.getChangeset();

            List<String> keys = new ArrayList<>(0);
            List<String> vals = new ArrayList<>(0);
            for (int j = 0; j < w.getKeysCount(); j++) {
                keys.add(getString(w.getKeys(j)));
                vals.add(getString(w.getVals(j)));
            }

            long nid = 0;
            List<Long> nodes = new ArrayList<>();
            for (int index = 0; index < w.getRefsCount(); index++) {
                nid = nid + w.getRefs(index);
                nodes.add(nid);
            }

            ways.add(new Way(id, version, uid, user, timestamp, changeset, nodes, keys, vals));
        }
        return ways;
    }

    public List<Relation> getRelations() {
        List<Relation> relations = new ArrayList<>();
        for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
            List<Osmformat.Relation> osmWays = group.getRelationsList();
            relations.addAll(getRelations(osmWays));
        }
        return relations;
    }

    protected List<Relation> getRelations(List<Osmformat.Relation> rels) {
        List<Relation> relations = new ArrayList<>();
        for (Osmformat.Relation r : rels) {
            long id = r.getId();

            Osmformat.Info info = r.getInfo();
            long timestamp = getTimestamp(info.getTimestamp());
            int version = info.getVersion();
            int uid = info.getUid();
            String user = getString(info.getUserSid());
            long changeset = info.getChangeset();

            List<String> keys = new ArrayList<>(0);
            List<String> vals = new ArrayList<>(0);
            for (int j = 0; j < r.getKeysCount(); j++) {
                keys.add(getString(r.getKeys(j)));
                vals.add(getString(r.getVals(j)));
            }

            long lastMid = 0;
            List<Member> members = new ArrayList<>();
            for (int j = 0; j < r.getMemidsCount(); j++) {
                long mid = lastMid + r.getMemids(j);
                lastMid = mid;
                String role = getString(r.getRolesSid(j));
                MemberType etype = null;
                if (r.getTypes(j) == Osmformat.Relation.MemberType.NODE) {
                    etype = MemberType.Node;
                } else if (r.getTypes(j) == Osmformat.Relation.MemberType.WAY) {
                    etype = MemberType.Way;
                } else if (r.getTypes(j) == Osmformat.Relation.MemberType.RELATION) {
                    etype = MemberType.Relation;
                } else {
                    assert false;
                }
                members.add(new Member(mid, etype, role));
            }
            relations.add(new Relation(id, version, uid, user, timestamp, changeset, members, keys, vals));
        }
        return relations;
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

    public static PrimitiveBlock read(Osmformat.PrimitiveBlock block) {
        return new PrimitiveBlockParser(block).read();
    }

}
