package io.gazetteer.osm.geometry;

import static io.gazetteer.osm.OSMTestUtil.COORDINATE_STORE;
import static io.gazetteer.osm.OSMTestUtil.COORDINATE_TRANSFORM;
import static io.gazetteer.osm.OSMTestUtil.GEOMETRY_FACTORY;
import static io.gazetteer.osm.OSMTestUtil.REFERENCE_STORE;
import static io.gazetteer.osm.OSMTestUtil.RELATION_1;
import static io.gazetteer.osm.OSMTestUtil.RELATION_0;
import static io.gazetteer.osm.OSMTestUtil.RELATION_2;
import static io.gazetteer.osm.OSMTestUtil.RELATION_3;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

class RelationBuilderTest {

  @Test
  void create() {
    RelationBuilder relationBuilder = new RelationBuilder(COORDINATE_TRANSFORM, GEOMETRY_FACTORY, COORDINATE_STORE, REFERENCE_STORE);
    assertNull(relationBuilder.build(RELATION_0));
    assertNull(relationBuilder.build(RELATION_1));
    assertTrue(relationBuilder.build(RELATION_2) instanceof Polygon);
    assertTrue(relationBuilder.build(RELATION_3) instanceof MultiPolygon);
  }
}
