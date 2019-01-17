package io.gazetteer.osm.postgis;

import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.domain.Way;
import io.gazetteer.osm.rocksdb.EntityStore;
import io.gazetteer.osm.rocksdb.EntityStoreException;
import mil.nga.sf.LineString;
import mil.nga.sf.Point;
import mil.nga.sf.Polygon;

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
        super("public", "ways");
        mapLong("id", getId);
        mapInteger("version", getVersion);
        mapInteger("uid", getUserId);
        mapLong("timestamp", getTimestamp);
        mapLong("changeset", getChangeset);
        mapHstore("tags", getTags);
        mapLongArray("nodes", getNodes);
        mapGeometry("geom", way -> {
            try {
                List<Long> ids = way.getNodes();
                if (ids.get(0).equals(ids.get(ids.size() - 1)) && ids.size() > 3) {
                    List<Node> nodes = cache.getAll(ids);
                    List<Point> points = new ArrayList<>();
                    for (Node node : nodes) {
                        points.add(new Point(node.getLon(), node.getLat()));
                    }
                    return new Polygon(new LineString(points));
                } else if (ids.size() > 1) {
                    List<Node> nodes = cache.getAll(ids);
                    List<Point> points = new ArrayList<>();
                    for (Node node : nodes) {
                        points.add(new Point(node.getLon(), node.getLat()));
                    }
                    return new LineString(points);
                } else {
                    return null;
                }
            } catch (EntityStoreException e) {
                throw new RuntimeException(e);
            }
        });
    }

}