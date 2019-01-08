package gazetteer.osm.model;

import java.util.List;

public class Node {

    private final long id;

    private final int version;

    private final int userId;

    private final long timestamp;

    private final long changesetId;

    private final double lon;

    private final double lat;

    private final List<String> keys;

    private final List<String> vals;

    public Node(long id, int version, int userId, long timestamp, long changesetId, double lon, double lat, List<String> keys, List<String> vals) {
        this.id = id;
        this.version = version;
        this.userId = userId;
        this.timestamp = timestamp;
        this.changesetId = changesetId;
        this.lon = lon;
        this.lat = lat;
        this.keys = keys;
        this.vals = vals;
    }

    public long getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public int getUserId() {
        return userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getChangesetId() {
        return changesetId;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public List<String> getKeys() {
        return keys;
    }

    public List<String> getVals() {
        return vals;
    }


}
