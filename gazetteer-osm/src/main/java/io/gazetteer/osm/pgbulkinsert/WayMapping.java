package io.gazetteer.osm.pgbulkinsert;

import io.gazetteer.osm.model.DataStore;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Way;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import static io.gazetteer.osm.postgis.GeometryUtil.asGeometryWithWrappedException;

public class WayMapping extends GeometryMapping<Way> {

  private final ToLongFunction<Way> getId = way -> way.getInfo().getId();

  private final ToIntFunction<Way> getVersion = way -> way.getInfo().getVersion();

  private final ToLongFunction<Way> getTimestamp = way -> way.getInfo().getChangeset();

  private final ToLongFunction<Way> getChangeset = way -> way.getInfo().getChangeset();

  private final ToIntFunction<Way> getUserId = way -> way.getInfo().getUserId();

  private final Function<Way, Map<String, String>> getTags = way -> way.getInfo().getTags();

  private final Function<Way, Collection<Long>> getNodes = way -> way.getNodes();

  public WayMapping(DataStore<Long, Node> nodeStore) {
    super("public", "osm_ways");
    mapLong("id", getId);
    mapInteger("version", getVersion);
    mapInteger("uid", getUserId);
    mapLong("timestamp", getTimestamp);
    mapLong("changeset", getChangeset);
    mapHstore("tags", getTags);
    mapLongArray("nodes", getNodes);
    mapGeometry("geom", way -> asGeometryWithWrappedException(way, nodeStore));
  }
}
