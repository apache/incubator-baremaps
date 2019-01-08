package gazetteer.osm.model;

import java.util.List;
import java.util.Map;

public class Way implements Entity {

    private final long id;

    private final List<Long> nodes;

    private final Map<String, String> tags;

    public Way(long id, List<Long> nodes, Map<String, String> tags) {
        this.id = id;
        this.nodes = nodes;
        this.tags = tags;
    }

    public long getId() {
        return id;
    }

    public List<Long> getNodes() {
        return nodes;
    }

    public Map<String, String> getTags() {
        return tags;
    }

}
