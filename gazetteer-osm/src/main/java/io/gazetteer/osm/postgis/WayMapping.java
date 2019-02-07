package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.EntityStore;
import io.gazetteer.osm.model.EntityStoreException;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.util.WrappedException;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class WayMapping extends GeometryMapping<Way> {

  private final ToLongFunction<Way> getId = way -> way.getInfo().getId();

  private final ToIntFunction<Way> getVersion = way -> way.getInfo().getVersion();

  private final ToLongFunction<Way> getTimestamp = way -> way.getInfo().getChangeset();

  private final ToLongFunction<Way> getChangeset = way -> way.getInfo().getChangeset();

  private final ToIntFunction<Way> getUserId = way -> way.getInfo().getUid();

  private final Function<Way, Map<String, String>> getTags = way -> way.getInfo().getTags();

  private final Function<Way, Collection<Long>> getNodes = way -> way.getNodes();

  public WayMapping(EntityStore<Node> cache) {
    super("public", "osm_ways");
    mapLong("id", getId);
    mapInteger("version", getVersion);
    mapInteger("uid", getUserId);
    mapLong("timestamp", getTimestamp);
    mapLong("changeset", getChangeset);
    mapHstore("tags", getTags);
    mapLongArray("nodes", getNodes);
    mapGeometry(
        "geom",
        way -> {
          try {
            return GeometryUtil.asGeometry(way, cache);
          } catch (EntityStoreException e) {
            throw new WrappedException(e);
          }
        });
  }
}
