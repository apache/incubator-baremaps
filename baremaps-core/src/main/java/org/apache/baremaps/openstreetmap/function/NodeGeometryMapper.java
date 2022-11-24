package org.apache.baremaps.openstreetmap.function;

import org.apache.baremaps.openstreetmap.model.Node;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * A function that adds a geometry to a node.
 */
public record NodeGeometryMapper(Context context) implements Function<Node, Node> {

  @Override
  public Node apply(Node node) {
    var point = context.geometryFactory().createPoint(new Coordinate(node.lon(), node.lat()));
    return node.withGeometry(point);
  }
}
