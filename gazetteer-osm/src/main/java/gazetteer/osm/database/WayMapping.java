package gazetteer.osm.database;

import gazetteer.osm.cache.EntityCache;
import gazetteer.osm.cache.EntityCacheException;
import gazetteer.osm.model.Node;
import gazetteer.osm.model.Way;
import mil.nga.sf.LineString;
import mil.nga.sf.Point;
import mil.nga.sf.Polygon;

import java.util.ArrayList;
import java.util.List;

public class WayMapping extends GeometryMapping<Way> {

    public WayMapping(EntityCache<Node> cache) {
        super("public", "ways");
        mapLong("id", Way::getId);
        mapInteger("version", Way::getVersion);
        mapInteger("user_id", Way::getUid);
        mapLong("tstamp", Way::getTimestamp);
        mapLong("changeset_id", Way::getChangeset);
        mapHstore("tags", Way::getTags);
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