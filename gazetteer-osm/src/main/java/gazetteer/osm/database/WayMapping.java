package gazetteer.osm.database;

import gazetteer.osm.cache.EntityCache;
import gazetteer.osm.model.Node;
import gazetteer.osm.model.Way;
import mil.nga.sf.LineString;
import mil.nga.sf.Point;
import mil.nga.sf.Polygon;

import java.util.List;
import java.util.stream.Collectors;

public class WayMapping extends GeometryMapping<Way> {

    public WayMapping(EntityCache<Node> cache) {
        super("public", "ways");
        mapLong("id", Way::getId);
        mapInteger("version", Way::getVersion);
        mapInteger("user_id", Way::getUserId);
        mapLong("tstamp", Way::getTimestamp);
        mapLong("changeset_id", Way::getChangesetId);
        mapHstore("tags", Way::getTags);
        mapGeometry("geom", way -> {
            List<Long> ids = way.getNodes();
            List<Node> nodes = cache.getAll(ids);
            List<Point> points = nodes.stream()
                    .map(n -> new Point(n.getLon(), n.getLat()))
                    .collect(Collectors.toList());
            boolean closed = ids.get(0).equals(ids.get(ids.size()-1));
            if (closed) {
                return new Polygon(new LineString(points));
            } else {
                return new LineString(points);
            }
        });
    }

}