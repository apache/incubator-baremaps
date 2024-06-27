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

package org.apache.baremaps.martini;

import java.awt.image.BufferedImage;

public class MartiniUtils {

  public static double[] mapboxTerrainToGrid(BufferedImage png) {
    int gridSize = png.getWidth() + 1;
    double[] terrain = new double[gridSize * gridSize];

    int tileSize = png.getWidth();

    // decode terrain values
    for (int y = 0; y < tileSize; y++) {
      for (int x = 0; x < tileSize; x++) {
        int k = (y * tileSize + x) * 4;
        int r = (png.getRGB(x, y) >> 16) & 0xFF;
        int g = (png.getRGB(x, y) >> 8) & 0xFF;
        int b = png.getRGB(x, y) & 0xFF;
        terrain[y * gridSize + x] = (r * 256 * 256 + g * 256.0f + b) / 10.0f - 10000.0f;
      }
    }

    // backfill right and bottom borders
    for (int x = 0; x < gridSize - 1; x++) {
      terrain[gridSize * (gridSize - 1) + x] = terrain[gridSize * (gridSize - 2) + x];
    }
    for (int y = 0; y < gridSize; y++) {
      terrain[gridSize * y + gridSize - 1] = terrain[gridSize * y + gridSize - 2];
    }

    return terrain;
  }
}
