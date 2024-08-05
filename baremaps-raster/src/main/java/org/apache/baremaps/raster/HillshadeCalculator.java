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

/**
 * Provides methods for generating hillshade effects on digital elevation models (DEMs).
 */
public class HillshadeCalculator {

  private double[] dem;
  private int width;
  private int height;
  private double cellSize;
  private double[] hillshade;

  public HillshadeCalculator(double[] dem, int width, int height, double cellSize) {
    this.dem = dem;
    this.width = width;
    this.height = height;
    this.cellSize = cellSize;
    this.hillshade = new double[width * height];
  }

  public double[] calculate(double altitude, double azimuth) {
    double azimuthDeg = 360.0 - azimuth + 90.0;
    double azimuthRad = Math.toRadians(azimuthDeg);

    double zenithDeg = 90 - altitude;
    double zenithRad = Math.toRadians(zenithDeg);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        double a = getElevation(x - 1, y - 1);
        double b = getElevation(x, y - 1);
        double c = getElevation(x + 1, y - 1);
        double d = getElevation(x - 1, y);
        double f = getElevation(x + 1, y);
        double g = getElevation(x - 1, y + 1);
        double h = getElevation(x, y + 1);
        double i = getElevation(x + 1, y + 1);

        double dzdx = ((c + 2 * f + i) - (a + 2 * d + g)) / (8 * cellSize);
        double dzdy = ((g + 2 * h + i) - (a + 2 * b + c)) / (8 * cellSize);

        double slopeRad = Math.atan(Math.sqrt(dzdx * dzdx + dzdy * dzdy));

        double aspectRad = Math.atan2(dzdy, -dzdx);
        if (aspectRad < 0) {
          aspectRad += 2 * Math.PI;
        }

        double hillshadeValue = 255.0
            * ((Math.cos(zenithRad) * Math.cos(slopeRad))
                + (Math.sin(zenithRad) * Math.sin(slopeRad) * Math.cos(azimuthRad - aspectRad)));

        hillshadeValue = Math.max(0, Math.min(255, hillshadeValue));

        hillshade[y * width + x] = hillshadeValue;
      }
    }

    return hillshade;
  }

  double getElevation(int x, int y) {
    x = Math.max(0, Math.min(width - 1, x));
    y = Math.max(0, Math.min(height - 1, y));
    return dem[y * width + x];
  }

  private static final double EARTH_RADIUS = 6378137; // in meters
  private static final int TILE_SIZE = 256; // in pixels

  public static double getResolution(int zoomLevel) {
    return (2 * Math.PI * EARTH_RADIUS) / (TILE_SIZE * Math.pow(2, zoomLevel));
  }
}
