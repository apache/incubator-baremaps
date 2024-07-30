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

import java.util.Arrays;
import org.locationtech.jts.geom.Coordinate;

public class ChaikinSmoother {

  private final Coordinate[] coordinates;
  private final double minX;
  private final double minY;
  private final double maxX;
  private final double maxY;
  private final boolean isOpen;

  public ChaikinSmoother(Coordinate[] coordinates, double minX, double minY, double maxX,
      double maxY) {
    this.coordinates = Arrays.copyOf(coordinates, coordinates.length);
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
    this.isOpen = !coordinates[0].equals(coordinates[coordinates.length - 1]);
  }

  public Coordinate[] smooth(int iterations, double factor) {
    Coordinate[] result = isOpen
        ? Arrays.copyOf(coordinates, coordinates.length - 1)
        : coordinates;

    double f1 = 1 - factor;
    double f2 = factor;

    // Apply the algorithm repeatedly
    for (int n = 0; n < iterations; n++) {
      Coordinate[] temp = new Coordinate[isOpen ? 2 * result.length - 2 : 2 * result.length];

      for (int i = 0; i < result.length; i++) {
        if (isOnBoundary(result[i]) || isOnBoundary(result[(i + 1) % result.length])) {
          temp[2 * i] = result[i];
          temp[2 * i + 1] = result[(i + 1) % result.length];
        } else {
          temp[2 * i] = new Coordinate(
              f1 * result[i].x + f2 * result[(i + 1) % result.length].x,
              f1 * result[i].y + f2 * result[(i + 1) % result.length].y);
          temp[2 * i + 1] = new Coordinate(
              f2 * result[i].x + f1 * result[(i + 1) % result.length].x,
              f2 * result[i].y + f1 * result[(i + 1) % result.length].y);
        }
      }

      if (isOpen) {
        temp[0] = result[0];
        temp[temp.length - 1] = result[result.length - 1];
      }

      result = temp;
    }

    if (!isOpen) {
      result = Arrays.copyOf(result, result.length + 1);
      result[result.length - 1] = result[0];
    }

    return result;
  }

  private boolean isOnBoundary(Coordinate coord) {
    return coord.x == minX || coord.x == maxX || coord.y == minY || coord.y == maxY;
  }
}
