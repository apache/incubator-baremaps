package io.gazetteer.osm.geometry;

import static io.gazetteer.osm.TestUtils.RELATION_0;
import static io.gazetteer.osm.TestUtils.RELATION_1;
import static io.gazetteer.osm.TestUtils.RELATION_2;
import static io.gazetteer.osm.TestUtils.RELATION_3;
import static io.gazetteer.osm.TestUtils.RELATION_4;
import static io.gazetteer.osm.TestUtils.RELATION_BUILDER;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

class RelationBuilderTest {

  @Test
  public void create() {
    assertNull(RELATION_BUILDER.build(RELATION_0));
    assertNull(RELATION_BUILDER.build(RELATION_1));
    assertTrue(RELATION_BUILDER.build(RELATION_2) instanceof Polygon);
    assertTrue(RELATION_BUILDER.build(RELATION_3) instanceof MultiPolygon);
    assertTrue(RELATION_BUILDER.build(RELATION_4) instanceof MultiPolygon);
  }
}
