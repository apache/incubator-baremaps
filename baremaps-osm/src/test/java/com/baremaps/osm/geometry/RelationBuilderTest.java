/*
 * Copyright (C) 2011 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.osm.geometry;

import static com.baremaps.osm.TestUtils.RELATION_0;
import static com.baremaps.osm.TestUtils.RELATION_1;
import static com.baremaps.osm.TestUtils.RELATION_2;
import static com.baremaps.osm.TestUtils.RELATION_3;
import static com.baremaps.osm.TestUtils.RELATION_4;
import static com.baremaps.osm.TestUtils.RELATION_BUILDER;
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
