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

package org.apache.baremaps.raster.hillshade;

public class HillShade {

  public static double[] hillShade(double[] dem, int width, int height, double sunAltitude,
      double sunAzimuth) {
    double[] hillshade = new double[dem.length];

    double scale = 0.1; // Adjust the scale factor if needed

    // Convert sun altitude and azimuth from degrees to radians
    double sunAltitudeRad = Math.toRadians(sunAltitude);
    double sunAzimuthRad = Math.toRadians(sunAzimuth + 90);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {

        // Handle edge cases for border pixels
        int top = Math.max(y - 1, 0);
        int left = Math.max(x - 1, 0);
        int bottom = Math.min(y + 1, height - 1);
        int right = Math.min(x + 1, width - 1);

        // Retrieve the elevation values from the 3x3 kernel
        double z2 = dem[top * width + x];
        double z4 = dem[y * width + left];
        double z6 = dem[y * width + right];
        double z8 = dem[bottom * width + x];

        // Calculate the dz/dx and dz/dy using the 3x3 kernel
        double dzdx = (z6 - z4) / 2.0;
        double dzdy = (z8 - z2) / 2.0;

        // Calculate the slope
        double slope = Math.atan(scale * Math.sqrt(dzdx * dzdx + dzdy * dzdy));

        // Calculate the aspect
        double aspect = Math.atan2(dzdy, dzdx);
        if (aspect < 0) {
          aspect += 2 * Math.PI;
        }

        // Calculate the reflectance
        double reflectance = Math.cos(sunAltitudeRad)
            * Math.cos(slope)
            + Math.sin(sunAltitudeRad)
                * Math.sin(slope)
                * Math.cos(sunAzimuthRad - aspect);

        // Normalize the reflectance to be between 0 and 255
        hillshade[y * width + x] = Math.max(0, Math.min(255, reflectance * 255));
      }
    }

    return hillshade;
  }

  public static double[] hillShadeEnhanced(double[] dem, int width, int height, double sunAltitude,
      double sunAzimuth) {
    double[] hillshade = new double[dem.length];

    double scale = 1.0; // Adjust the scale factor if needed

    // Convert sun altitude and azimuth from degrees to radians
    double sunAltitudeRad = Math.toRadians(sunAltitude);
    double sunAzimuthRad = Math.toRadians(sunAzimuth);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {

        // Handle edge cases for border pixels
        int top = Math.max(y - 1, 0);
        int bottom = Math.min(y + 1, height - 1);
        int left = Math.max(x - 1, 0);
        int right = Math.min(x + 1, width - 1);

        // Retrieve the elevation values from the 3x3 kernel
        double z1 = dem[top * width + left];
        double z2 = dem[top * width + x];
        double z3 = dem[top * width + right];
        double z4 = dem[y * width + left];
        double z6 = dem[y * width + right];
        double z7 = dem[bottom * width + left];
        double z8 = dem[bottom * width + x];
        double z9 = dem[bottom * width + right];

        // Calculate the dz/dx and dz/dy using the 3x3 kernel
        double dzdx = ((z3 + 2 * z6 + z9) - (z1 + 2 * z4 + z7)) / 8.0;
        double dzdy = ((z7 + 2 * z8 + z9) - (z1 + 2 * z2 + z3)) / 8.0;

        // Calculate the slope
        double slope = Math.atan(scale * Math.sqrt(dzdx * dzdx + dzdy * dzdy));

        // Calculate the aspect
        double aspect = Math.atan2(dzdy, -dzdx);
        if (aspect < 0) {
          aspect += 2 * Math.PI;
        }

        // Calculate the reflectance
        double reflectance = Math.cos(sunAltitudeRad)
            * Math.cos(slope)
            + Math.sin(sunAltitudeRad)
                * Math.sin(slope)
                * Math.cos(sunAzimuthRad - aspect);

        // Normalize the reflectance to be between 0 and 255
        hillshade[y * width + x] = Math.max(0, Math.min(255, reflectance * 255));
      }
    }

    return hillshade;
  }
}
