package gazetteer.osm.model;

import java.util.List;
import java.util.Map;

public class Way implements Entity {

    private final long id;

    private final int version;

    private final int userId;

    private final long timestamp;

    private final long changesetId;

    private final List<Long> nodes;

    private final Map<String, String> tags;

    public Way(long id, int version, int userId, long timestamp, long changesetId, List<Long> nodes, Map<String, String> tags) {
        this.id = id;
        this.version = version;
        this.userId = userId;
        this.timestamp = timestamp;
        this.changesetId = changesetId;
        this.nodes = nodes;
        this.tags = tags;
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

    public List<Long> getNodes() {
        return nodes;
    }

    public Map<String, String> getTags() {
        return tags;
    }


}
