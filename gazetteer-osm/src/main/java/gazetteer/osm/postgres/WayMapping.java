package gazetteer.osm.postgres;

import de.bytefish.pgbulkinsert.mapping.AbstractMapping;
import de.bytefish.pgbulkinsert.pgsql.model.geometric.Path;
import de.bytefish.pgbulkinsert.pgsql.model.geometric.Point;
import gazetteer.osm.leveldb.DataStore;
import gazetteer.osm.model.Node;
import gazetteer.osm.model.Way;

import java.util.List;
import java.util.stream.Collectors;

public class WayMapping extends AbstractMapping<Way> {

    public WayMapping(DataStore<Node> cache) {
        super("public", "ways");
        mapLong("id", Way::getId);
        mapPath("geom", way -> {
            List<Long> ids = way.getNodes();
            List<Node> nodes = cache.getAll(ids);
            List<Point> points = nodes.stream()
                    .map(n -> new Point(n.getLon(), n.getLat()))
                    .collect(Collectors.toList());
            boolean closed = ids.get(0).equals(ids.get(ids.size()-1));
            return new Path(closed, points);
        });
        mapHstore("tags", Way::getTags);
    }

}