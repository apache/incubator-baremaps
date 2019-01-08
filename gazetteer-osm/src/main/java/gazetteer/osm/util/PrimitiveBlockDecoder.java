package gazetteer.osm.util;

import gazetteer.osm.model.Node;
import gazetteer.osm.model.Way;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrimitiveBlockDecoder {

    private Osmformat.PrimitiveBlock block;

    private int granularity;
    private int dateGranularity;
    private long latOffset;
    private long lonOffset;
    private String stringTable[];

    public PrimitiveBlockDecoder(Osmformat.PrimitiveBlock block) {
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

    public Map<Long, Node> getNodes() {
        Map<Long, Node> nodes = new HashMap<>();
        for (Osmformat.PrimitiveGroup groupmessage : block.getPrimitivegroupList()) {
            Osmformat.DenseNodes dense = groupmessage.getDense();
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
                nodes.put(id, new Node(id, version,userId, timestamp, changeset,lon, lat, keys, vals));
            }
        }
        return nodes;
    }

    public Map<Long, Way> getWays() {
        HashMap<Long, Way> ways = new HashMap<>();
        for (Osmformat.PrimitiveGroup groupmessage : block.getPrimitivegroupList()) {
            for (Osmformat.Way way : groupmessage.getWaysList()) {
                Map<String, String> tags = new HashMap<>();
                ways.put(way.getId(), new Way(way.getId(), way.getRefsList(), tags));
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
