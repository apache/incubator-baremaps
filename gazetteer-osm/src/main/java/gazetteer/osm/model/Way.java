package gazetteer.osm.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Way implements Entity {

    private final long id;

    private final int version;

    private final int uid;

    private final String user;

    private final long timestamp;

    private final long changeset;

    private final List<Long> nodes;

    private final List<String> keys;

    private final List<String> vals;

    public Way(long id, int version, int uid, String user, long timestamp, long changeset, List<Long> nodes, List<String> keys, List<String> vals) {
        this.id = id;
        this.version = version;
        this.uid = uid;
        this.user = user;
        this.timestamp = timestamp;
        this.changeset = changeset;
        this.nodes = nodes;
        this.keys = keys;
        this.vals = vals;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public int getUid() {
        return uid;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public long getChangeset() {
        return changeset;
    }

    public List<Long> getNodes() {
        return nodes;
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
