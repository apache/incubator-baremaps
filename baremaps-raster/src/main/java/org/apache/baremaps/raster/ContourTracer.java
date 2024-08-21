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

package org.apache.baremaps.raster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.jts.operation.linemerge.LineMerger;

/**
 * Provides methods for generating contour lines and contour polygons from digital elevation models
 * (DEMs).
 */
public class ContourTracer {

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  private static final double EPSILON = 1e-10;

  private final double[] grid;

  private final int width;

  private final int height;

  private final boolean normalize;

  private final boolean polygonize;

  /**
   * Constructs a new ContourTracer with the specified grid, width, height, normalization, and
   * polygonization options.
   *
   * @param grid The grid of elevation values
   * @param width The width of the grid
   * @param height The height of the grid
   * @param normalize Whether to normalize the coordinates
   * @param polygonize Whether to polygonize the contours
   */
  public ContourTracer(double[] grid, int width, int height, boolean normalize,
      boolean polygonize) {
    this.grid = Arrays.copyOf(grid, grid.length);
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
    List<LineString> cells = new ArrayList<>();
    for (int y = 0; y < height - 1; y++) {
      for (int x = 0; x < width - 1; x++) {
        cells.addAll(processCell(level, x, y));
      }
    }

    // Merge the cells
    LineMerger cellMerger = new LineMerger();
    cellMerger.add(cells);
    List<Geometry> contours = new ArrayList<>(cellMerger.getMergedLineStrings());

    // Polygonize the lines
    if (polygonize) {
      contours = contours.stream()
          .map(Geometry::getCoordinates)
          .map(GEOMETRY_FACTORY::createPolygon)
          .map(Geometry.class::cast)
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
   * Generates contour for a given range of elevation levels.
   *
   * @param start The starting elevation level (inclusive)
   * @param end The ending elevation level (exclusive)
   * @param interval The interval between elevation levels
   * @return A list of contour geometries
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

  @SuppressWarnings("squid:S3776")
  private List<LineString> processCell(double level, int x, int y) {
    List<LineString> segments = new ArrayList<>();

    boolean htb = polygonize && y == height - 2;
    boolean hrb = polygonize && x == width - 2;
    boolean hbb = polygonize && y == 0;
    boolean hlb = polygonize && x == 0;

    Coordinate tlc = new Coordinate(x, y + 1.0);
    Coordinate tmc = interpolateCoordinate(level, x, y + 1, x + 1, y + 1);
    Coordinate trc = new Coordinate(x + 1.0, y + 1.0);
    Coordinate mrc = interpolateCoordinate(level, x + 1, y, x + 1, y + 1);
    Coordinate brc = new Coordinate(x + 1.0, y);
    Coordinate bmc = interpolateCoordinate(level, x, y, x + 1, y);
    Coordinate blc = new Coordinate(x, y);
    Coordinate mlc = interpolateCoordinate(level, x, y, x, y + 1);

    double tlv = grid[y * width + x];
    double trv = grid[y * width + (x + 1)];
    double brv = grid[(y + 1) * width + (x + 1)];
    double blv = grid[(y + 1) * width + x];
    double avg = (tlv + trv + brv + blv) / 4.0;

    int index =
        (tlv >= level ? 1 : 0) |
            (trv >= level ? 2 : 0) |
            (brv >= level ? 4 : 0) |
            (blv >= level ? 8 : 0);

    switch (index) {
      case 1 -> {
        segments.add(createSegment(mlc, bmc));
        if (hbb) {
          segments.add(createSegment(bmc, blc));
        }
        if (hlb) {
          segments.add(createSegment(blc, mlc));
        }
      }
      case 2 -> {
        segments.add(createSegment(bmc, mrc));
        if (hrb) {
          segments.add(createSegment(mrc, brc));
        }
        if (hbb) {
          segments.add(createSegment(brc, bmc));
        }
      }
      case 3 -> {
        segments.add(createSegment(mlc, mrc));
        if (hrb) {
          segments.add(createSegment(mrc, brc));
        }
        if (hbb) {
          segments.add(createSegment(brc, blc));
        }
        if (hlb) {
          segments.add(createSegment(blc, mlc));
        }
      }
      case 4 -> {
        segments.add(createSegment(mrc, tmc));
        if (htb) {
          segments.add(createSegment(tmc, trc));
        }
        if (hrb) {
          segments.add(createSegment(trc, mrc));
        }
      }
      case 5 -> {
        segments.add(createSegment(mlc, tmc));
        if (htb) {
          segments.add(createSegment(tmc, trc));
        }
        if (hrb) {
          segments.add(createSegment(trc, mrc));
        }
        segments.add(createSegment(mrc, bmc));
        if (hbb) {
          segments.add(createSegment(bmc, blc));
        }
        if (hlb) {
          segments.add(createSegment(blc, mlc));
        }
      }
      case 6 -> {
        segments.add(createSegment(bmc, tmc));
        if (htb) {
          segments.add(createSegment(tmc, trc));
        }
        if (hrb) {
          segments.add(createSegment(trc, brc));
        }
        if (hbb) {
          segments.add(createSegment(brc, bmc));
        }
      }
      case 7 -> {
        segments.add(createSegment(mlc, tmc));
        if (htb) {
          segments.add(createSegment(tmc, trc));
        }
        if (hrb) {
          segments.add(createSegment(trc, brc));
        }
        if (hbb) {
          segments.add(createSegment(brc, blc));
        }
        if (hlb) {
          segments.add(createSegment(blc, mlc));
        }
      }
      case 8 -> {
        segments.add(createSegment(tmc, mlc));
        if (hlb) {
          segments.add(createSegment(mlc, tlc));
        }
        if (htb) {
          segments.add(createSegment(tlc, tmc));
        }
      }
      case 9 -> {
        segments.add(createSegment(tmc, bmc));
        if (hbb) {
          segments.add(createSegment(bmc, blc));
        }
        if (hlb) {
          segments.add(createSegment(blc, tlc));
        }
        if (htb) {
          segments.add(createSegment(tlc, tmc));
        }
      }
      case 10 -> {
        // Detect saddle points ambiguity
        segments.add(createSegment(bmc, mlc));
        if (hlb) {
          segments.add(createSegment(mlc, tlc));
        }
        if (htb) {
          segments.add(createSegment(tlc, tmc));
        }
        segments.add(createSegment(tmc, mrc));
        if (hrb) {
          segments.add(createSegment(mrc, brc));
        }
        if (hbb) {
          segments.add(createSegment(brc, bmc));
        }
      }
      case 11 -> {
        segments.add(createSegment(tmc, mrc));
        if (hrb) {
          segments.add(createSegment(mrc, brc));
        }
        if (hbb) {
          segments.add(createSegment(brc, blc));
        }
        if (hlb) {
          segments.add(createSegment(blc, tlc));
        }
        if (htb) {
          segments.add(createSegment(tlc, tmc));
        }
      }
      case 12 -> {
        segments.add(createSegment(mrc, mlc));
        if (hlb) {
          segments.add(createSegment(mlc, tlc));
        }
        if (htb) {
          segments.add(createSegment(tlc, trc));
        }
        if (hrb) {
          segments.add(createSegment(trc, mrc));
        }
      }
      case 13 -> {
        segments.add(createSegment(mrc, bmc));
        if (hbb) {
          segments.add(createSegment(bmc, blc));
        }
        if (hlb) {
          segments.add(createSegment(blc, tlc));
        }
        if (htb) {
          segments.add(createSegment(tlc, trc));
        }
        if (hrb) {
          segments.add(createSegment(trc, mrc));
        }
      }
      case 14 -> {
        segments.add(createSegment(bmc, mlc));
        if (hlb) {
          segments.add(createSegment(mlc, tlc));
        }
        if (htb) {
          segments.add(createSegment(tlc, trc));
        }
        if (hrb) {
          segments.add(createSegment(trc, brc));
        }
        if (hbb) {
          segments.add(createSegment(brc, bmc));
        }
      }
      case 15 -> {
        if (htb) {
          segments.add(createSegment(tlc, trc));
        }
        if (hrb) {
          segments.add(createSegment(trc, brc));
        }
        if (hbb) {
          segments.add(createSegment(brc, blc));
        }
        if (hlb) {
          segments.add(createSegment(blc, tlc));
        }
      }
      default -> {
        // No segments
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
    if (t < EPSILON) {
      t = EPSILON;
    } else if (t > 1 - EPSILON) {
      t = 1 - EPSILON;
    }
    double x = x1 + t * (x2 - x1);
    double y = y1 + t * (y2 - y1);
    return new Coordinate(x, y);
  }

  private class NormalizationTransformer extends GeometryTransformer {

    @Override
    protected CoordinateSequence transformCoordinates(CoordinateSequence coords, Geometry parent) {
      for (int i = 0; i < coords.size(); i++) {
        Coordinate coordinate = coords.getCoordinate(i);
        double x = coordinate.getX() / width * (width + 1);
        double y = coordinate.getY() / height * (height + 1);
        coords.setOrdinate(i, 0, x);
        coords.setOrdinate(i, 1, y);
      }
      return coords;
    }
  }
}
