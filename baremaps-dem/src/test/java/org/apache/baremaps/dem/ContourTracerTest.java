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

import static org.apache.baremaps.testing.GeometryAssertions.assertGeometryEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;

class ContourTracerTest {

  @Test
  @DisplayName("Test grid normalization")
  void testGrid1() throws ParseException {
    var grid = new double[] {
        0, 0, 0,
        0, 1, 0,
        0, 0, 0,
    };
    var generatedContour = new ContourTracer(grid, 3, 3, true, true).traceContours(0).get(0);
    assertGeometryEquals(
        "POLYGON ((0 1.3333333333333333, 0 2.6666666666666665, 1.3333333333333333 2.6666666666666665, 2.6666666666666665 2.6666666666666665, 2.6666666666666665 1.3333333333333333, 2.6666666666666665 0, 1.3333333333333333 0, 0 0, 0 1.3333333333333333))",
        generatedContour);
  }

  @Test
  @DisplayName("Test Mount Fuji")
  void testMountFuji() throws IOException {
    var fujiImage = ImageIO.read(
        Path.of("")
            .toAbsolutePath()
            .resolveSibling("baremaps-dem/src/test/resources/fuji.png")
            .toAbsolutePath().toFile());

    var fujiGrid = ElevationUtils.imageToGrid(fujiImage, ElevationUtils::pixelToElevationNormal);
    var fujiContours =
        new ContourTracer(fujiGrid, fujiImage.getWidth(), fujiImage.getHeight(), false, true)
            .traceContours(500);

    assertFalse(fujiContours.isEmpty());
  }

  @Disabled
  @Test
  @DisplayName("Test polygon with hole")
  void testPolygonWithHole() {
    var grid = new double[] {
        0, 0, 0, 0, 0,
        0, 1, 1, 1, 0,
        0, 1, 0, 1, 0,
        0, 1, 1, 1, 0,
        0, 0, 0, 0, 0,
    };
    var generatedContours = new ContourTracer(grid, 5, 5, false, true).traceContours(0.5);
    var polygon = (Polygon) generatedContours.get(0);
    var exteriorRing = polygon.getExteriorRing();
    var interiorRing = polygon.getInteriorRingN(0);
    assertGeometryEquals("LINEARRING(0.5 1, 0.5 2, 0.5 3, 1 3.5, 2 3.5, 3 3.5, 3.5 3, 3.5 2, 3.5 1, 3 0.5, 2 0.5, 1 0.5, 0.5 1)", exteriorRing);
    assertGeometryEquals("LINEARRING(1.5 1.5, 1.5 2.5, 2.5 2.5, 2.5 1.5, 1.5 1.5)", interiorRing);
  }

}
