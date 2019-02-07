package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.Entity;
import io.gazetteer.osm.model.Node;

import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class NodeMapping extends GeometryMapping<Node> {

  private final ToIntFunction<Node> getVersion = node -> node.getInfo().getVersion();

  private final ToLongFunction<Node> getTimestamp = node -> node.getInfo().getChangeset();

  private final ToLongFunction<Node> getChangeset = node -> node.getInfo().getChangeset();

  private final ToIntFunction<Node> getUserId = node -> node.getInfo().getUid();

  private final Function<Node, Map<String, String>> getTags = node -> node.getInfo().getTags();

  public NodeMapping() {
    super("public", "osm_nodes");
    mapLong("id", (ToLongFunction<Node>) NodeMapping::getId);
    mapInteger("version", getVersion);
    mapInteger("uid", getUserId);
    mapLong("timestamp", getTimestamp);
    mapLong("changeset", getChangeset);
    mapHstore("tags", getTags);
    mapGeometry("geom", GeometryUtil::asGeometry);
  }

  public static final <E extends Entity> Long getId(E entity) {
    return entity.getInfo().getId();
  }
}
