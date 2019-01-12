package gazetteer.osm.database;

import gazetteer.osm.model.Node;
import mil.nga.sf.Point;

import java.util.HashMap;
import java.util.Map;

public class NodeMapping extends GeometryMapping<Node> {

    public NodeMapping() {
        super("public", "nodes");
        mapLong("id", Node::getId);
        mapInteger("version", Node::getVersion);
        mapInteger("user_id", Node::getUid);
        mapLong("tstamp", Node::getTimestamp);
        mapLong("changeset_id", Node::getChangeset);
        mapHstore("tags", node -> {
            Map<String, String> tags = new HashMap<>();
            for (int i = 0; i < node.getKeys().size(); i++) {
                tags.put(node.getKeys().get(i), node.getVals().get(i));
            }
            return tags;
        });
        mapGeometry("geom", node -> new Point(node.getLon(), node.getLat()));
    }

}
