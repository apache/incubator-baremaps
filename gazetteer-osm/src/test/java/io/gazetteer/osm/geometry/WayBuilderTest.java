package io.gazetteer.osm.geometry;

import static io.gazetteer.osm.OSMTestUtil.COORDINATE_STORE;
import static io.gazetteer.osm.OSMTestUtil.COORDINATE_TRANSFORM;
import static io.gazetteer.osm.OSMTestUtil.GEOMETRY_FACTORY;
import static io.gazetteer.osm.OSMTestUtil.WAY_LINESTRING;
import static io.gazetteer.osm.OSMTestUtil.WAY_POLYGON_OUTER_1;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

class WayBuilderTest {

  @Test
  void create() {
    WayBuilder wayBuilder = new WayBuilder(COORDINATE_TRANSFORM, GEOMETRY_FACTORY, COORDINATE_STORE);
    Geometry linestring = wayBuilder.build(WAY_LINESTRING);
    assertTrue(linestring instanceof LineString);
    Geometry polygon = wayBuilder.build(WAY_POLYGON_OUTER_1);
    assertTrue(polygon instanceof Polygon);
  }
}
