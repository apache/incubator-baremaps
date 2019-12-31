package io.gazetteer.osm.geometry;

import static io.gazetteer.osm.TestUtils.COORDINATE_STORE;
import static io.gazetteer.osm.TestUtils.COORDINATE_TRANSFORM;
import static io.gazetteer.osm.TestUtils.GEOMETRY_FACTORY;
import static io.gazetteer.osm.TestUtils.WAY_0;
import static io.gazetteer.osm.TestUtils.WAY_1;
import static io.gazetteer.osm.TestUtils.WAY_2;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

class WayBuilderTest {

  @Test
  public void create() {
    WayBuilder wayBuilder = new WayBuilder(COORDINATE_TRANSFORM, GEOMETRY_FACTORY, COORDINATE_STORE);
    Geometry empty = wayBuilder.build(WAY_0);
    assertNull(empty);
    Geometry linestring = wayBuilder.build(WAY_1);
    assertTrue(linestring instanceof LineString);
    Geometry polygon = wayBuilder.build(WAY_2);
    assertTrue(polygon instanceof Polygon);
  }
}
