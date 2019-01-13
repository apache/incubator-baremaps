package gazetteer.osm.database;

import gazetteer.osm.model.Node;
import mil.nga.sf.Geometry;
import mil.nga.sf.Point;

import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class NodeMapping extends GeometryMapping<Node> {

    private final ToLongFunction<Node> getId = node -> node.getData().getId();
    private final ToIntFunction<Node> getVersion = node -> node.getData().getVersion();
    private final ToLongFunction<Node> getTimestamp = node -> node.getData().getChangeset();
    private final ToLongFunction<Node> getChangeset = node -> node.getData().getChangeset();
    private final ToIntFunction<Node> getUserId = node -> node.getData().getUser().getId();
    private final Function<Node, Map<String, String>> getTags = node -> node.getData().getTags();
    private final Function<Node, Geometry> getGeometry = node -> new Point(node.getLon(), node.getLat());

    public NodeMapping() {
        super("public", "nodes");
        mapLong("id", getId);
        mapInteger("version", getVersion);
        mapInteger("user_id", getUserId);
        mapLong("tstamp", getTimestamp);
        mapLong("changeset_id", getChangeset);
        mapHstore("tags", getTags);
        mapGeometry("geom", getGeometry);
    }

}
