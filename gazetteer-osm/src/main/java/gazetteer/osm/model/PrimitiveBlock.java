package gazetteer.osm.model;

import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.util.*;

public class PrimitiveBlock {

    private Osmformat.PrimitiveBlock block;

    private int granularity;
    private int dateGranularity;
    private long latOffset;
    private long lonOffset;
    private String stringTable[];

    public PrimitiveBlock(Osmformat.PrimitiveBlock block) {
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

    public List<Node> getNodes() {
        ArrayList<Node> nodes = new ArrayList<>();
        for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
            Osmformat.DenseNodes dense = group.getDense();
            Osmformat.DenseInfo info = dense.getDenseinfo();
            long id = 0;
            int tag = 0;
            for (int i = 0; i < dense.getIdCount(); i++) {
                id += dense.getId(i);
                int version = info.getVersion(i);
                int userId = info.getUid(i);
                long timestamp = getTimestamp(info.getTimestamp(i));
                long changeset = info.getChangeset(i);
                double lat = getLat(dense.getLat(i));
                double lon = getLon(dense.getLon(i));
                List<String> keys = new ArrayList<>();
                List<String> vals = new ArrayList<>();
                if (dense.getKeysVals(tag) == 0) {
                    tag += 1;
                } else {
                    while (dense.getKeysVals(tag) != 0) {
                        keys.add(getString(dense.getKeysVals(tag)));
                        vals.add(getString(dense.getKeysVals(tag + 1)));
                        tag += 2;
                    }
                }
                nodes.add(new Node(id, version,userId, timestamp, changeset,lon, lat, keys, vals));
            }
        }
        return nodes;
    }

    public List<Way> getWays() {
        List<Way> ways = new ArrayList<>();
        for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
            long id = 0;
            for (Osmformat.Way way : group.getWaysList()) {
                id += way.getId();
                Osmformat.Info info = way.getInfo();
                int version = info.getVersion();
                int userId = info.getUid();
                long timestamp = info.getTimestamp();
                long changesetId = info.getChangeset();
                Map<String, String> tags = new HashMap<>();
                for (int i = 0; i < way.getKeysCount(); i++) {
                    tags.put(getString(way.getKeys(i)), getString(way.getVals(i)));
                }


                ways.add(new Way(way.getId(), version, userId, timestamp, changesetId, way.getRefsList(), tags));
            }
        }
        return ways;
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
