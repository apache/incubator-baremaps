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

package org.apache.baremaps.raster.contour;

import java.util.ArrayList;
import java.util.List;

public class IsoLines {

  public record Point(double x, double y) {
  }

  public record IsoLine(List<Point> points) {
  }

  public static List<IsoLine> isoLines(double[] grid, int gridSize, double level) {
    List<IsoLine> isoLines = new ArrayList<>();
    for (int y = 0; y < gridSize - 1; y++) {
      for (int x = 0; x < gridSize - 1; x++) {
        int index = (grid[y * (gridSize + 1) + x] > level ? 1 : 0) |
            (grid[y * (gridSize + 1) + (x + 1)] > level ? 2 : 0) |
            (grid[(y + 1) * (gridSize + 1) + (x + 1)] > level ? 4 : 0) |
            (grid[(y + 1) * (gridSize + 1) + x] > level ? 8 : 0);
        List<Point> points = new ArrayList<>();
        switch (index) {
          case 1:
          case 14:
            points.add(interpolate(x, y, x, y + 1, grid, gridSize + 1, level));
            points.add(interpolate(x, y, x + 1, y, grid, gridSize + 1, level));
            break;
          case 2:
          case 13:
            points.add(interpolate(x + 1, y, x, y, grid, gridSize + 1, level));
            points.add(interpolate(x + 1, y, x + 1, y + 1, grid, gridSize + 1, level));
            break;
          case 3:
          case 12:
            points.add(interpolate(x, y, x, y + 1, grid, gridSize + 1, level));
            points.add(interpolate(x + 1, y, x + 1, y + 1, grid, gridSize + 1, level));
            break;
          case 4:
          case 11:
            points.add(interpolate(x + 1, y + 1, x + 1, y, grid, gridSize + 1, level));
            points.add(interpolate(x + 1, y + 1, x, y + 1, grid, gridSize + 1, level));
            break;
          case 5:
            points.add(interpolate(x, y, x, y + 1, grid, gridSize + 1, level));
            points.add(interpolate(x, y + 1, x + 1, y + 1, grid, gridSize + 1, level));
            points.add(interpolate(x + 1, y, x + 1, y + 1, grid, gridSize + 1, level));
            points.add(interpolate(x + 1, y, x, y, grid, gridSize + 1, level));
            break;
          case 6:
          case 9:
            points.add(interpolate(x, y, x + 1, y, grid, gridSize + 1, level));
            points.add(interpolate(x, y + 1, x + 1, y + 1, grid, gridSize + 1, level));
            break;
          case 7:
          case 8:
            points.add(interpolate(x, y, x, y + 1, grid, gridSize + 1, level));
            points.add(interpolate(x, y + 1, x + 1, y + 1, grid, gridSize + 1, level));
            break;
          case 10:
            points.add(interpolate(x, y, x + 1, y, grid, gridSize + 1, level));
            points.add(interpolate(x + 1, y, x + 1, y + 1, grid, gridSize + 1, level));
            points.add(interpolate(x + 1, y + 1, x, y + 1, grid, gridSize + 1, level));
            points.add(interpolate(x, y + 1, x, y, grid, gridSize + 1, level));
            break;
        }
        if (!points.isEmpty()) {
          isoLines.add(new IsoLine(points));
        }
      }
    }
    return isoLines;
  }

  public static List<IsoLine> isoLines(double[] grid, int gridSize, int start, int end,
      int interval) {
    List<IsoLine> isoLines = new ArrayList<>();
    for (int level = start; level < end; level++) {
      isoLines.addAll(isoLines(grid, gridSize, level));
    }
    return isoLines;
  }

  private static Point interpolate(
      int x1,
      int y1,
      int x2,
      int y2,
      double[] grid,
      int width,
      double level) {
    double v1 = grid[y1 * width + x1];
    double v2 = grid[y2 * width + x2];
    double t = (level - v1) / (v2 - v1);
    return new Point(x1 + t * (x2 - x1), y1 + t * (y2 - y1));
  }

}
