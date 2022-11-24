package org.apache.baremaps.openstreetmap.function;

import org.apache.baremaps.openstreetmap.OsmReaderContext;
import org.apache.baremaps.openstreetmap.model.Node;
import org.locationtech.jts.geom.Coordinate;

import java.util.function.Function;

/**
 * A function that adds a geometry to a node.
 */
public record NodeGeometryMapper(OsmReaderContext context) implements Function<Node, Node> {

  @Override
  public Node apply(Node node) {
    var point = context.geometryFactory().createPoint(new Coordinate(node.lon(), node.lat()));
    return node.withGeometry(point);
  }
}
