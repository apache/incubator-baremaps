package gazetteer.osm.postgis;

import gazetteer.osm.rocksdb.EntityCache;
import gazetteer.osm.rocksdb.EntityCacheException;
import gazetteer.osm.domain.Node;
import gazetteer.osm.domain.Way;
import mil.nga.sf.LineString;
import mil.nga.sf.Point;
import mil.nga.sf.Polygon;

import java.util.ArrayList;
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

    public WayMapping(EntityCache<Node> cache) {
        super("public", "ways");
        mapLong("id", getId);
        mapInteger("version", getVersion);
        mapInteger("user_id", getUserId);
        mapLong("tstamp", getTimestamp);
        mapLong("changeset_id", getChangeset);
        mapHstore("tags", getTags);
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
            } catch (EntityCacheException e) {
                throw new RuntimeException();
            }
        });
    }

}