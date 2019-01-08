package gazetteer.osm.postgres;

import de.bytefish.pgbulkinsert.mapping.AbstractMapping;
import de.bytefish.pgbulkinsert.pgsql.model.geometric.Point;
import gazetteer.osm.model.Node;

import java.util.HashMap;
import java.util.Map;

public class NodeMapping extends AbstractMapping<Node> {

    public NodeMapping() {
        super("public", "nodes");
        mapLong("id", Node::getId);
        mapInteger("version", Node::getVersion);
        mapInteger("user_id", Node::getUserId);
        mapLong("tstamp", Node::getTimestamp);
        mapLong("changeset_id", Node::getChangesetId);
        mapPoint("geom", node -> new Point(node.getLon(), node.getLat()));
        mapHstore("tags", node -> {
            Map<String, String> tags = new HashMap<>();
            for (int i = 0; i < node.getKeys().size(); i++) {
                tags.put(node.getKeys().get(i), node.getVals().get(i));
            }
            return tags;
        });
    }

}
