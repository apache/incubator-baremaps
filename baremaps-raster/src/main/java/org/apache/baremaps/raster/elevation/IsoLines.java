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

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.linemerge.LineMerger;

/**
 * Provides methods for generating isoline contours from digital elevation models (DEMs).
 */
public class IsoLines {

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  private static final double EPSILON = 1e-10;

  private IsoLines() {
    // Prevent instantiation
  }

  /**
   * Generates isolines for a given grid at a specific level.
   *
   * @param grid The elevation data
   * @param width The width of the grid
   * @param height The height of the grid
   * @param level The elevation level for which to generate isolines
   * @param normalize Whether to normalize the coordinates
   * @return A list of LineString objects representing the isolines
   */
  public static List<LineString> generateIsoLines(
      double[] grid, int width, int height,
      double level, boolean normalize) {
    validateInput(grid, width, height);
    List<LineString> lineStrings = new ArrayList<>();
    for (int y = 0; y < height - 1; y++) {
      for (int x = 0; x < width - 1; x++) {
        processCell(grid, width, height, level, normalize, lineStrings, y, x);
      }
    }
    return mergeLineStrings(lineStrings);
  }

  /**
   * Generates isolines for a given grid at multiple levels within a specified range.
   *
   * @param grid The elevation data
   * @param width The width of the grid
   * @param height The height of the grid
   * @param start The starting elevation level
   * @param end The ending elevation level
   * @param interval The interval between elevation levels
   * @param normalize Whether to normalize the coordinates
   * @return A list of LineString objects representing the isolines
   */
  public static List<LineString> generateIsoLines(
      double[] grid, int width, int height,
      int start, int end, int interval,
      boolean normalize) {
    validateInput(grid, width, height);
    List<LineString> isoLines = new ArrayList<>();
    for (int level = start; level < end; level += interval) {
      isoLines.addAll(generateIsoLines(grid, width, height, level, normalize));
    }
    return isoLines;
  }

  private static List<LineString> mergeLineStrings(List<LineString> lineStrings) {
    LineMerger lineMerger = new LineMerger();
    lineMerger.add(lineStrings);
    return new ArrayList<>(lineMerger.getMergedLineStrings());
  }

  private static void validateInput(double[] grid, int width, int height) {
    if (grid == null || grid.length == 0) {
      throw new IllegalArgumentException("Grid array cannot be null or empty");
    }
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Width and height must be positive");
    }
    if (grid.length != width * height) {
      throw new IllegalArgumentException("Grid array length does not match width * height");
    }
  }

  private static void processCell(
      double[] grid, int width, int height,
      double level, boolean normalize, List<LineString> lineStrings,
      int y, int x) {
    double tl = grid[y * width + x];
    double tr = grid[y * width + (x + 1)];
    double br = grid[(y + 1) * width + (x + 1)];
    double bl = grid[(y + 1) * width + x];

    int index =
        (tl > level ? 1 : 0) |
            (tr > level ? 2 : 0) |
            (br > level ? 4 : 0) |
            (bl > level ? 8 : 0);

    switch (index) {
      case 1:
      case 14:
        createLineString(
            grid, width, height, level, normalize, lineStrings,
            x, y, x + 1, y,
            x, y + 1, x, y);
        break;
      case 2:
      case 13:
        createLineString(
            grid, width, height, level, normalize, lineStrings,
            x + 1, y, x, y,
            x + 1, y, x + 1, y + 1);
        break;
      case 3:
      case 12:
        createLineString(
            grid, width, height, level, normalize, lineStrings,
            x, y, x, y + 1,
            x + 1, y, x + 1, y + 1);
        break;
      case 4:
      case 11:
        createLineString(
            grid, width, height, level, normalize, lineStrings,
            x + 1, y + 1, x + 1, y,
            x + 1, y + 1, x, y + 1);
        break;
      case 5:
        createLineString(
            grid, width, height, level, normalize, lineStrings,
            x, y, x, y + 1,
            x, y + 1, x + 1, y + 1);
        createLineString(
            grid, width, height, level, normalize, lineStrings,
            x + 1, y, x + 1, y + 1,
            x + 1, y, x, y);
        break;
      case 6:
      case 9:
        createLineString(
            grid, width, height, level, normalize, lineStrings,
            x, y, x + 1, y,
            x, y + 1, x + 1, y + 1);
        break;
      case 7:
      case 8:
        createLineString(
            grid, width, height, level, normalize, lineStrings,
            x, y, x, y + 1,
            x, y + 1, x + 1, y + 1);
        break;
      case 10:
        createLineString(
            grid, width, height, level, normalize, lineStrings,
            x, y, x + 1, y,
            x + 1, y, x + 1, y + 1);
        createLineString(
            grid, width, height, level, normalize, lineStrings,
            x + 1, y + 1, x, y + 1,
            x, y + 1, x, y);
        break;
    }
  }

  private static void createLineString(
      double[] grid, int width, int height,
      double level, boolean normalize, List<LineString> lineStrings,
      int x1, int y1, int x2, int y2,
      int x3, int y3, int x4, int y4) {
    Coordinate c1 = interpolate(grid, width, height, level, normalize, x1, y1, x2, y2);
    Coordinate c2 = interpolate(grid, width, height, level, normalize, x3, y3, x4, y4);
    lineStrings.add(GEOMETRY_FACTORY.createLineString(new Coordinate[] {c1, c2}));
  }

  private static Coordinate interpolate(
      double[] grid, int width, int height,
      double level, boolean normalize,
      int x1, int y1, int x2, int y2) {
    double v1 = grid[y1 * width + x1];
    double v2 = grid[y2 * width + x2];
    double t = (Math.abs(v2 - v1) < EPSILON) ? 0.5 : (level - v1) / (v2 - v1);
    double x = x1 + t * (x2 - x1);
    double y = y1 + t * (y2 - y1);
    if (normalize) {
      x = x / (width - 1) * width;
      y = y / (height - 1) * height;
    }
    return new Coordinate(x, y);
  }
}
