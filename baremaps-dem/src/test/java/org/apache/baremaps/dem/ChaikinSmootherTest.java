/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.dem;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

class ChaikinSmootherTest {

  @Test
  void smoothLineString() {
    LineString lineString = new GeometryFactory().createLineString(new Coordinate[] {
        new Coordinate(0, 0),
        new Coordinate(1, 1),
    });
    Geometry smoothedLineString = new ChaikinSmoother(2, 0.25).transform(lineString);
    assertEquals("LINESTRING (0 0, 0.375 0.375, 0.625 0.625, 0.75 0.75, 1 1)",
        smoothedLineString.toString());
  }
}
