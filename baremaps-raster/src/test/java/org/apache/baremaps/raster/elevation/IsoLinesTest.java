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

package org.apache.baremaps.raster.elevation;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

class IsoLinesTest {

  @Test
  @DisplayName("Test simple grid")
  void testSimpleGrid() throws ParseException {
    var grid = new double[] {
        0, 0, 0,
        0, 1, 0,
        0, 0, 0,
    };
    var expectedContour = new WKTReader().read("LINESTRING (0 1.5, 1.5 3, 3 1.5, 1.5 0, 0 1.5)");
    var generatedContour = IsoLines.generateIsoLines(grid, 3, 3, 0, true).get(0);
    assertEquals(expectedContour, generatedContour);
  }

  @Test
  void testMountFuji() throws IOException {
    var fujiImage = ImageIO.read(
        Path.of("")
            .toAbsolutePath()
            .resolveSibling("baremaps-raster/src/test/resources/fuji.png")
            .toAbsolutePath().toFile());
    var fujiGrid = ElevationUtils.imageToGrid(fujiImage);
    var fujiContours =
        IsoLines.generateIsoLines(fujiGrid, fujiImage.getWidth(), fujiImage.getHeight(), 500, true);
    assertFalse(fujiContours.isEmpty());
  }
}
