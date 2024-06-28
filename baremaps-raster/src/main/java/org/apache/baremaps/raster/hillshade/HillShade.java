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

  public static double[] hillshade(double[] grid, int gridSize, double altitude, double azimuth) {
    double[] hillshade = new double[grid.length];

    // Convert altitude and azimuth to radians
    double altitudeRad = Math.toRadians(altitude);
    double azimuthRad = Math.toRadians(360.0 - azimuth + 90.0); // Convert azimuth to mathematical
                                                                // coordinates

    // Precompute sine and cosine of altitude and azimuth for the light source
    double sinAltitude = Math.sin(altitudeRad);
    double cosAltitude = Math.cos(altitudeRad);
    double sinAzimuth = Math.sin(azimuthRad);
    double cosAzimuth = Math.cos(azimuthRad);

    for (int y = 1; y < gridSize - 1; y++) {
      for (int x = 1; x < gridSize - 1; x++) {
        int index = y * gridSize + x;

        // Calculate the gradients dz/dx and dz/dy using central difference
        double dzdx = (grid[y * gridSize + (x + 1)] - grid[y * gridSize + (x - 1)]) / 2.0;
        double dzdy = (grid[(y + 1) * gridSize + x] - grid[(y - 1) * gridSize + x]) / 2.0;

        // Calculate the slope and aspect
        double slope = Math.atan(Math.sqrt(dzdx * dzdx + dzdy * dzdy));
        double aspect = Math.atan2(dzdy, -dzdx);

        // Calculate the illumination angle
        double hillshadeValue = 255.0 * (sinAltitude * Math.cos(slope) +
            cosAltitude * Math.sin(slope) *
                (cosAzimuth * Math.cos(aspect) + sinAzimuth * Math.sin(aspect)));

        hillshade[index] = Math.max(0, Math.min(255, hillshadeValue));
      }
    }

    // Handle the border cells (set them to zero or some other handling strategy)
    for (int i = 0; i < gridSize; i++) {
      hillshade[i] = 0; // Top border
      hillshade[(gridSize - 1) * gridSize + i] = 0; // Bottom border
      hillshade[i * gridSize] = 0; // Left border
      hillshade[i * gridSize + (gridSize - 1)] = 0; // Right border
    }

    return hillshade;
  }


}
