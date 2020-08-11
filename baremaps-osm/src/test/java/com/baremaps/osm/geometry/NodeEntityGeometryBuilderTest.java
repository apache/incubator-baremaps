/*
 * Copyright (C) 2020 The Baremaps Authors
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

import static com.baremaps.osm.geometry.GeometryConstants.NODE_BUILDER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

public class NodeEntityGeometryBuilderTest {

  @Test
  public void build() {
    Point p0 = NODE_BUILDER.build(GeometryConstants.NODE_0);
    assertEquals(p0.getX(), 0);
    assertEquals(p0.getY(), 0);
    Point p1 = NODE_BUILDER.build(GeometryConstants.NODE_2);
    assertEquals(p1.getX(), 3);
    assertEquals(p1.getY(), 3);
  }
}
