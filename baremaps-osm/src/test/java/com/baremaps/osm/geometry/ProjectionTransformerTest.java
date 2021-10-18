package com.baremaps.osm.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

class ProjectionTransformerTest {

  private static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

  private static final ProjectionTransformer TRANSFORMER = new ProjectionTransformer(4326, 3857);

  @Test
  public void testPoint() {
    var inputGeom = FACTORY.createPoint(new Coordinate(1, 1));
    var outputGeom = (Point) TRANSFORMER.transform(inputGeom);
    assertEquals(9804, outputGeom.getSRID());
    assertEquals(111319.49079327357, outputGeom.getX());
    assertEquals(111325.14286638486, outputGeom.getY());
  }

}