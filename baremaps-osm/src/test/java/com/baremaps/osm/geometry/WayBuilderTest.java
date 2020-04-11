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

import static com.baremaps.osm.TestUtils.COORDINATE_STORE;
import static com.baremaps.osm.TestUtils.COORDINATE_TRANSFORM;
import static com.baremaps.osm.TestUtils.GEOMETRY_FACTORY;
import static com.baremaps.osm.TestUtils.WAY_0;
import static com.baremaps.osm.TestUtils.WAY_1;
import static com.baremaps.osm.TestUtils.WAY_2;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

class WayBuilderTest {

  @Test
  public void create() {
    // TODO: improve this test
    /*
    WayBuilder wayBuilder = new WayBuilder(COORDINATE_TRANSFORM, GEOMETRY_FACTORY, COORDINATE_STORE);
    Geometry empty = wayBuilder.build(WAY_0);
    assertNull(empty);
    Geometry linestring = wayBuilder.build(WAY_1);
    assertTrue(linestring instanceof LineString);
    Geometry polygon = wayBuilder.build(WAY_2);
    assertTrue(polygon instanceof Polygon);
    */
  }
}
