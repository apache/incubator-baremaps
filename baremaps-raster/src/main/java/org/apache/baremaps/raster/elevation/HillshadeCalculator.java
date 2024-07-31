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

/**
 * Provides methods for generating hillshade effects on digital elevation models (DEMs).
 */
public class HillshadeCalculator {

  private static final double DEFAULT_SCALE = 0.1;
  private static final double ENHANCED_SCALE = 1.0;
  private static final double MIN_REFLECTANCE = 0.0;
  private static final double MAX_REFLECTANCE = 255.0;
  private static final double TWO_PI = 2 * Math.PI;

  private final double[] dem;
  private final int width;
  private final int height;
  private final double scale;
  private final boolean isSimple;

  public HillshadeCalculator(double[] dem, int width, int height, double scale, boolean isSimple) {
    this.dem = dem;
    this.width = width;
    this.height = height;
    this.scale = scale;
    this.isSimple = isSimple;
  }

  /**
   * Generates a hillshade effect on the DEM.
   *
   * @param sunAltitude The sun's altitude in degrees
   * @param sunAzimuth The sun's azimuth in degrees
   * @return An array representing the hillshade effect
   */
  public double[] calculate(double sunAltitude, double sunAzimuth) {
    validateInput(dem, width, height, sunAltitude, sunAzimuth);
    return calculateHillshade(sunAltitude, sunAzimuth);
  }

  private static void validateInput(double[] dem, int width, int height, double sunAltitude,
      double sunAzimuth) {
    if (dem == null || dem.length == 0) {
      throw new IllegalArgumentException("DEM array cannot be null or empty");
    }
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Width and height must be positive");
    }
    if (dem.length != width * height) {
      throw new IllegalArgumentException("DEM array length does not match width * height");
    }
    if (sunAltitude < 0 || sunAltitude > 90) {
      throw new IllegalArgumentException("Sun altitude must be between 0 and 90 degrees");
    }
    if (sunAzimuth < 0 || sunAzimuth > 360) {
      throw new IllegalArgumentException("Sun azimuth must be between 0 and 360 degrees");
    }
  }

  private double[] calculateHillshade(double sunAltitude, double sunAzimuth) {
    double[] hillshade = new double[dem.length];

    double sunAltitudeRad = Math.toRadians(sunAltitude);
    double sunAzimuthRad = Math.toRadians(sunAzimuth + (isSimple ? 90 : 180));
    double cosSunAltitude = Math.cos(sunAltitudeRad);
    double sinSunAltitude = Math.sin(sunAltitudeRad);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int top = Math.max(y - 1, 0);
        int bottom = Math.min(y + 1, height - 1);
        int left = Math.max(x - 1, 0);
        int right = Math.min(x + 1, width - 1);

        double dzdx;
        double dzdy;
        if (isSimple) {
          dzdx = (dem[y * width + right] - dem[y * width + left]) / 2.0;
          dzdy = (dem[bottom * width + x] - dem[top * width + x]) / 2.0;
        } else {
          dzdx = ((dem[top * width + right] + 2 * dem[y * width + right]
              + dem[bottom * width + right]) -
              (dem[top * width + left] + 2 * dem[y * width + left] + dem[bottom * width + left]))
              / 8.0;
          dzdy = ((dem[bottom * width + left] + 2 * dem[bottom * width + x]
              + dem[bottom * width + right]) -
              (dem[top * width + left] + 2 * dem[top * width + x] + dem[top * width + right]))
              / 8.0;
        }

        double slope = Math.atan(scale * Math.hypot(dzdx, dzdy));
        double aspect = Math.atan2(dzdy, isSimple ? dzdx : -dzdx);
        if (aspect < 0) {
          aspect += TWO_PI;
        }

        double reflectance = cosSunAltitude * Math.cos(slope) +
            sinSunAltitude * Math.sin(slope) * Math.cos(sunAzimuthRad - aspect);

        hillshade[y * width + x] =
            Math.max(MIN_REFLECTANCE, Math.min(MAX_REFLECTANCE, reflectance * MAX_REFLECTANCE));
      }
    }

    return hillshade;
  }

}
