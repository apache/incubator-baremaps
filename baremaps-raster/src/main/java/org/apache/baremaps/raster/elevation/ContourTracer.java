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
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.jts.operation.linemerge.LineMerger;

/**
 * Provides methods for generating contour polygons from digital elevation models (DEMs).
 */
public class ContourTracer {

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  private static final double EPSILON = 1e-10;

  private final double[] grid;

  private final int width;

  private final int height;

  private final boolean normalize;

  private final boolean polygonize;

  public ContourTracer(double[] grid, int width, int height, boolean normalize,
      boolean polygonize) {
    this.grid = grid;
    this.width = width;
    this.height = height;
    this.normalize = normalize;
    this.polygonize = polygonize;
  }

  /**
   * Generates isolines for a given grid at a specific level.
   *
   * @param level The elevation level for which to generate isolines
   * @return A list of LineString objects representing the isolines
   */
  public List<Geometry> traceContours(double level) {
    validateInput(grid, width, height);

    // Process each cell in the grid to generate segments
    List<LineString> segments = new ArrayList<>();
    for (int y = 0; y < height - 1; y++) {
      for (int x = 0; x < width - 1; x++) {
        segments.addAll(processCell(level, x, y));
      }
    }

    // Merge segments into line strings
    LineMerger segmentMerger = new LineMerger();
    segmentMerger.add(segments);
    List<Geometry> contours = new ArrayList<>(segmentMerger.getMergedLineStrings());

    // Polygonize the line strings
    if (polygonize) {
      contours = contours.stream()
          .map(geometry -> (Geometry) GEOMETRY_FACTORY.createPolygon(geometry.getCoordinates()))
          .toList();
    }

    // Normalize the coordinates
    if (normalize) {
      NormalizationTransformer transformer = new NormalizationTransformer();
      contours = contours.stream()
          .map(geometry -> transformer.transform(geometry.copy()))
          .toList();
    }

    return contours;
  }

  /**
   * Generates isolines for a given grid at multiple levels within a specified range.
   *
   * @param start The starting elevation level
   * @param end The ending elevation level
   * @param interval The interval between elevation levels
   * @return A list of LineString objects representing the isolines
   */
  public List<Geometry> traceContours(int start, int end, int interval) {
    validateInput(grid, width, height);
    List<Geometry> contours = new ArrayList<>();
    for (int level = start; level < end; level += interval) {
      contours.addAll(traceContours(level));
    }
    return contours;
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

  protected List<LineString> processCell(double level, int x, int y) {
    List<LineString> segments = new ArrayList<>();

    double tl = grid[y * width + x];
    double tr = grid[y * width + (x + 1)];
    double br = grid[(y + 1) * width + (x + 1)];
    double bl = grid[(y + 1) * width + x];

    int index =
        (tl > level ? 1 : 0) |
            (tr > level ? 2 : 0) |
            (br > level ? 4 : 0) |
            (bl > level ? 8 : 0);

    Coordinate tlc = new Coordinate(x, y + 1);
    Coordinate tmc = interpolateCoordinate(level, x, y + 1, x + 1, y + 1);
    Coordinate trc = new Coordinate(x + 1, y + 1);
    Coordinate mrc = interpolateCoordinate(level, x + 1, y, x + 1, y + 1);
    Coordinate brc = new Coordinate(x + 1, y);
    Coordinate bmc = interpolateCoordinate(level, x, y, x + 1, y);
    Coordinate blc = new Coordinate(x, y);
    Coordinate mlc = interpolateCoordinate(level, x, y, x, y + 1);

    boolean ht = polygonize && y == height - 2;
    boolean hr = polygonize && x == width - 2;
    boolean hb = polygonize && y == 0;
    boolean hl = polygonize && x == 0;

    switch (index) {
      case 1 -> {
        segments.add(createSegment(mlc, bmc));
        if (hb) {
          segments.add(createSegment(bmc, blc));
        }
        if (hl) {
          segments.add(createSegment(blc, mlc));
        }
      }
      case 2 -> {
        segments.add(createSegment(bmc, mrc));
        if (hr) {
          segments.add(createSegment(mrc, brc));
        }
        if (hb) {
          segments.add(createSegment(brc, bmc));
        }
      }
      case 3 -> {
        segments.add(createSegment(mlc, mrc));
        if (hr) {
          segments.add(createSegment(mrc, brc));
        }
        if (hb) {
          segments.add(createSegment(brc, blc));
        }
        if (hl) {
          segments.add(createSegment(blc, mlc));
        }
      }
      case 4 -> {
        segments.add(createSegment(mrc, tmc));
        if (ht) {
          segments.add(createSegment(tmc, trc));
        }
        if (hr) {
          segments.add(createSegment(trc, mrc));
        }
      }
      case 5 -> {
        segments.add(createSegment(mlc, tmc));
        if (ht) {
          segments.add(createSegment(tmc, trc));
        }
        if (hr) {
          segments.add(createSegment(trc, mrc));
        }
        segments.add(createSegment(mrc, bmc));
        if (hb) {
          segments.add(createSegment(bmc, blc));
        }
        if (hl) {
          segments.add(createSegment(blc, mlc));
        }
      }
      case 6 -> {
        segments.add(createSegment(bmc, tmc));
        if (ht) {
          segments.add(createSegment(tmc, trc));
        }
        if (hr) {
          segments.add(createSegment(trc, brc));
        }
        if (hb) {
          segments.add(createSegment(brc, bmc));
        }
      }
      case 7 -> {
        segments.add(createSegment(mlc, tmc));
        if (ht) {
          segments.add(createSegment(tmc, trc));
        }
        if (hr) {
          segments.add(createSegment(trc, brc));
        }
        if (hb) {
          segments.add(createSegment(brc, blc));
        }
        if (hl) {
          segments.add(createSegment(blc, mlc));
        }
      }
      case 8 -> {
        segments.add(createSegment(tmc, mlc));
        if (hl) {
          segments.add(createSegment(mlc, tlc));
        }
        if (ht) {
          segments.add(createSegment(tlc, tmc));
        }
      }
      case 9 -> {
        segments.add(createSegment(tmc, bmc));
        if (hb) {
          segments.add(createSegment(bmc, blc));
        }
        if (hl) {
          segments.add(createSegment(blc, tlc));
        }
        if (ht) {
          segments.add(createSegment(tlc, tmc));
        }
      }
      case 10 -> {
        segments.add(createSegment(bmc, mlc));
        if (hl) {
          segments.add(createSegment(mlc, tlc));
        }
        if (ht) {
          segments.add(createSegment(tlc, tmc));
        }
        segments.add(createSegment(tmc, mrc));
        if (hr) {
          segments.add(createSegment(mrc, brc));
        }
        if (hb) {
          segments.add(createSegment(brc, bmc));
        }
      }
      case 11 -> {
        segments.add(createSegment(tmc, mrc));
        if (hr) {
          segments.add(createSegment(mrc, brc));
        }
        if (hb) {
          segments.add(createSegment(brc, blc));
        }
        if (hl) {
          segments.add(createSegment(blc, tlc));
        }
        if (ht) {
          segments.add(createSegment(tlc, tmc));
        }
      }
      case 12 -> {
        segments.add(createSegment(mrc, mlc));
        if (hl) {
          segments.add(createSegment(mlc, tlc));
        }
        if (ht) {
          segments.add(createSegment(tlc, trc));
        }
        if (hr) {
          segments.add(createSegment(trc, mrc));
        }
      }
      case 13 -> {
        segments.add(createSegment(mrc, bmc));
        if (hb) {
          segments.add(createSegment(bmc, blc));
        }
        if (hl) {
          segments.add(createSegment(blc, tlc));
        }
        if (ht) {
          segments.add(createSegment(tlc, trc));
        }
        if (hr) {
          segments.add(createSegment(trc, mrc));
        }
      }
      case 14 -> {
        segments.add(createSegment(bmc, mlc));
        if (hl) {
          segments.add(createSegment(mlc, tlc));
        }
        if (ht) {
          segments.add(createSegment(tlc, trc));
        }
        if (hr) {
          segments.add(createSegment(trc, brc));
        }
        if (hb) {
          segments.add(createSegment(brc, bmc));
        }
      }
      case 15 -> {
        if (ht) {
          segments.add(createSegment(tlc, trc));
        }
        if (hr) {
          segments.add(createSegment(trc, brc));
        }
        if (hb) {
          segments.add(createSegment(brc, blc));
        }
        if (hl) {
          segments.add(createSegment(blc, tlc));
        }
      }
    }

    return segments;
  }

  private LineString createSegment(Coordinate c1, Coordinate c2) {
    return GEOMETRY_FACTORY.createLineString(new Coordinate[] {c1, c2});
  }

  private Coordinate interpolateCoordinate(double level, int x1, int y1, int x2, int y2) {
    double v1 = grid[y1 * width + x1];
    double v2 = grid[y2 * width + x2];
    double t = (Math.abs(v2 - v1) < EPSILON) ? 0.5 : (level - v1) / (v2 - v1);
    double x = x1 + t * (x2 - x1);
    double y = y1 + t * (y2 - y1);
    return new Coordinate(x, y);
  }

  private class NormalizationTransformer extends GeometryTransformer {

    @Override
    protected CoordinateSequence transformCoordinates(CoordinateSequence coords, Geometry parent) {
      for (int i = 0; i < coords.size(); i++) {
        Coordinate coord = coords.getCoordinate(i);
        double x = coord.getX() / (width - 1) * width;
        double y = coord.getY() / (height - 1) * height;
        coords.setOrdinate(i, 0, x);
        coords.setOrdinate(i, 1, y);
      }
      return coords;
    }
  }
}
