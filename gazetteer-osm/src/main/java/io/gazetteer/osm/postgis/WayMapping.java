package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.EntityStore;
import io.gazetteer.osm.model.EntityStoreException;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.util.WrappedException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class WayMapping extends GeometryMapping<Way> {

  private final ToLongFunction<Way> getId = way -> way.getInfo().getId();

  private final ToIntFunction<Way> getVersion = way -> way.getInfo().getVersion();

  private final ToLongFunction<Way> getTimestamp = way -> way.getInfo().getChangeset();

  private final ToLongFunction<Way> getChangeset = way -> way.getInfo().getChangeset();

  private final ToIntFunction<Way> getUserId = way -> way.getInfo().getUser().getId();

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
            GeometryFactory geometryFactory = new GeometryFactory();
            List<Long> ids = way.getNodes();
            if (ids.get(0).equals(ids.get(ids.size() - 1)) && ids.size() > 3) {
              List<Node> nodes = cache.getAll(ids);
              List<Coordinate> points = new ArrayList<>();
              for (Node node : nodes) {
                points.add(new Coordinate(node.getLon(), node.getLat()));
              }
              return geometryFactory.createPolygon(points.toArray(new Coordinate[0]));
            } else if (ids.size() > 1) {
              List<Node> nodes = cache.getAll(ids);
              List<Coordinate> points = new ArrayList<>();
              for (Node node : nodes) {
                points.add(new Coordinate(node.getLon(), node.getLat()));
              }
              return geometryFactory.createLineString(points.toArray(new Coordinate[0]));
            } else {
              return null;
            }
          } catch (EntityStoreException e) {
            throw new WrappedException(e);
          }
        });
  }
}
