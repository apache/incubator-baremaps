package org.apache.baremaps.openstreetmap.function;

import org.apache.baremaps.openstreetmap.model.Way;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

/**
 * A function that adds a geometry to a way.
 */
public record WayGeometryMapper(Context context) implements Function<Way, Way> {

  private static final Logger logger = LoggerFactory.getLogger(WayGeometryMapper.class);

  @Override
  public Way apply(Way way) {
    try {
      List<Coordinate> list = way.nodes().stream().map(context.coordinateMap()::get).toList();
      Coordinate[] array = list.toArray(new Coordinate[list.size()]);
      LineString line = context.geometryFactory().createLineString(array);
      if (!line.isEmpty()) {
        if (!line.isClosed()) {
          return way.withGeometry(line);
        } else {
          Polygon polygon = context.geometryFactory().createPolygon(line.getCoordinates());
          return way.withGeometry(polygon);
        }
      }
    } catch (Exception e) {
      logger.warn("Unable to build the geometry for way #" + way.id(), e);
    }
    return way;
  }
}
