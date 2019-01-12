package gazetteer.osm.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node implements Entity {

    private final long id;

    private final int version;

    private final int uid;

    private final String user;

    private final long timestamp;

    private final long changeset;

    private final double lon;

    private final double lat;

    private final List<String> keys;

    private final List<String> vals;

    public Node(long id, int version, int uid, String user, long timestamp, long changeset, double lon, double lat, List<String> keys, List<String> vals) {
        this.id = id;
        this.version = version;
        this.uid = uid;
        this.user = user;
        this.timestamp = timestamp;
        this.changeset = changeset;
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

    public int getUid() {
        return uid;
    }

    public String getUser() {
        return user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getChangeset() {
        return changeset;
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

    public Map<String, String> getTags() {
        Map<String, String> tags = new HashMap<>();
        for (int t = 0; t < keys.size(); t++) {
            tags.put(keys.get(t), vals.get(t));
        }
        return tags;
    }


}
