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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.LineString;

class HillShadeTest {

  private static final double DELTA = 1e-6;

  @Test
  @DisplayName("Test hillShade with valid input")
  void testHillShadeValidInput() {
    double[] dem = {
        1, 2, 3,
        4, 5, 6,
        7, 8, 9
    };
    int width = 3;
    int height = 3;
    double sunAltitude = 45;
    double sunAzimuth = 315;

    double[] result = HillShade.hillShade(dem, width, height, sunAltitude, sunAzimuth);

    assertNotNull(result);
    assertEquals(dem.length, result.length);
  }

  @Test
  @DisplayName("Test hillShadeEnhanced with valid input")
  void testHillShadeEnhancedValidInput() {
    double[] dem = {
        1, 2, 3,
        4, 5, 6,
        7, 8, 9
    };
    int width = 3;
    int height = 3;
    double sunAltitude = 45;
    double sunAzimuth = 315;

    double[] result = HillShade.hillShadeEnhanced(dem, width, height, sunAltitude, sunAzimuth);

    assertNotNull(result);
    assertEquals(dem.length, result.length);
  }

  @ParameterizedTest
  @MethodSource("provideInvalidInput")
  @DisplayName("Test hillShade with invalid input")
  void testHillShadeInvalidInput(double[] dem, int width, int height, double sunAltitude,
      double sunAzimuth, Class<? extends Exception> expectedException) {
    assertThrows(expectedException,
        () -> HillShade.hillShade(dem, width, height, sunAltitude, sunAzimuth));
  }

  @ParameterizedTest
  @MethodSource("provideInvalidInput")
  @DisplayName("Test hillShadeEnhanced with invalid input")
  void testHillShadeEnhancedInvalidInput(double[] dem, int width, int height, double sunAltitude,
      double sunAzimuth, Class<? extends Exception> expectedException) {
    assertThrows(expectedException,
        () -> HillShade.hillShadeEnhanced(dem, width, height, sunAltitude, sunAzimuth));
  }

  private static Stream<Arguments> provideInvalidInput() {
    return Stream.of(
        Arguments.of(null, 3, 3, 45, 315, IllegalArgumentException.class),
        Arguments.of(new double[0], 3, 3, 45, 315, IllegalArgumentException.class),
        Arguments.of(new double[9], 0, 3, 45, 315, IllegalArgumentException.class),
        Arguments.of(new double[9], 3, 0, 45, 315, IllegalArgumentException.class),
        Arguments.of(new double[9], 2, 2, 45, 315, IllegalArgumentException.class),
        Arguments.of(new double[9], 3, 3, -1, 315, IllegalArgumentException.class),
        Arguments.of(new double[9], 3, 3, 91, 315, IllegalArgumentException.class),
        Arguments.of(new double[9], 3, 3, 45, -1, IllegalArgumentException.class),
        Arguments.of(new double[9], 3, 3, 45, 361, IllegalArgumentException.class));
  }

  @Test
  @DisplayName("Test hillShade output range")
  void testHillShadeOutputRange() {
    double[] dem = new double[100];
    for (int i = 0; i < dem.length; i++) {
      dem[i] = Math.random() * 1000;
    }
    int width = 10;
    int height = 10;
    double sunAltitude = 45;
    double sunAzimuth = 315;

    double[] result = HillShade.hillShade(dem, width, height, sunAltitude, sunAzimuth);

    for (double value : result) {
      assertTrue(value >= 0 && value <= 255, "Hillshade value should be between 0 and 255");
    }
  }

  @Test
  @DisplayName("Test hillShadeEnhanced output range")
  void testHillShadeEnhancedOutputRange() {
    double[] dem = new double[100];
    for (int i = 0; i < dem.length; i++) {
      dem[i] = Math.random() * 1000;
    }
    int width = 10;
    int height = 10;
    double sunAltitude = 45;
    double sunAzimuth = 315;

    double[] result = HillShade.hillShadeEnhanced(dem, width, height, sunAltitude, sunAzimuth);

    for (double value : result) {
      assertTrue(value >= 0 && value <= 255, "Hillshade value should be between 0 and 255");
    }
  }

  @Test
  @DisplayName("Test hillShade with fuji.png")
  void testHillShadeWithFujiPng(@TempDir Path tempDir) throws IOException {
    Path imagePath = Path.of("")
        .toAbsolutePath()
        .resolveSibling("baremaps-raster/src/test/resources/fuji.png")
        .toAbsolutePath();
    var png = ImageIO.read(imagePath.toFile());
    assertNotNull(png, "Failed to load test image");

    var grid = ElevationUtils.imageToGrid(png);
    int width = png.getWidth();
    int height = png.getHeight();
    double sunAltitude = 45;
    double sunAzimuth = 315;

    var hillshade = HillShade.hillShade(grid, width, height, sunAltitude, sunAzimuth);

    assertNotNull(hillshade, "Hillshade result should not be null");
    assertEquals(width * height, hillshade.length,
        "Hillshade array size should match image dimensions");

    var isoLines = new ArrayList<LineString>();
    for (int i = 0; i < 255; i += 50) {
      List<LineString> lines = IsoLines.generateIsoLines(hillshade, width, height, i, true);
      assertNotNull(lines, "Isoline generation should not return null");
      isoLines.addAll(lines);
    }

    assertFalse(isoLines.isEmpty(), "At least one isoline should be generated");

    BufferedImage hillshadeImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int gray = (int) hillshade[y * width + x];
        hillshadeImage.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
      }
    }
    Path outputPath = tempDir.resolve("fuji_hillshade.png");
    ImageIO.write(hillshadeImage, "png", outputPath.toFile());
    assertTrue(outputPath.toFile().exists(), "Hillshade image should be saved");
  }
}
