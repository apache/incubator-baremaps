package io.gazetteer.osm.postgis;

import io.gazetteer.osm.domain.Node;
import mil.nga.sf.Geometry;
import mil.nga.sf.Point;

import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class NodeMapping extends GeometryMapping<Node> {

    private final ToLongFunction<Node> getId = node -> node.getInfo().getId();
    private final ToIntFunction<Node> getVersion = node -> node.getInfo().getVersion();
    private final ToLongFunction<Node> getTimestamp = node -> node.getInfo().getChangeset();
    private final ToLongFunction<Node> getChangeset = node -> node.getInfo().getChangeset();
    private final ToIntFunction<Node> getUserId = node -> node.getInfo().getUser().getId();
    private final Function<Node, Map<String, String>> getTags = node -> node.getInfo().getTags();
    private final Function<Node, Geometry> getGeometry = node -> new Point(node.getLon(), node.getLat());

    public NodeMapping() {
        super("public", "nodes");
        mapLong("id", getId);
        mapInteger("version", getVersion);
        mapInteger("uid", getUserId);
        mapLong("timestamp", getTimestamp);
        mapLong("changeset", getChangeset);
        mapHstore("tags", getTags);
        mapGeometry("geom", getGeometry);
    }

}
