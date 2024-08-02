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

import java.awt.image.BufferedImage;
import java.util.function.DoubleToIntFunction;
import java.util.function.IntToDoubleFunction;

/**
 * Provides utility methods for processing raster images, particularly for elevation data.
 */
public class ElevationUtils {

  private static final double ELEVATION_SCALE = 256.0 * 256.0;
  private static final double ELEVATION_OFFSET = 10000.0;

  private ElevationUtils() {
    // Private constructor to prevent instantiation
  }

  /**
   * Converts a BufferedImage to a grid of elevation values.
   *
   * @param image The input BufferedImage
   * @return A double array representing the elevation grid
   */
  public static double[] imageToGrid(BufferedImage image,
      IntToDoubleFunction pixelToElevation) {
    validateImage(image);
    int width = image.getWidth();
    int height = image.getHeight();
    double[] grid = new double[width * height];

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int rgb = image.getRGB(x, y);
        grid[y * width + x] = pixelToElevation.applyAsDouble(rgb);
      }
    }

    return grid;
  }

  /**
   * Converts a grid of elevation values to a BufferedImage.
   *
   * @param grid The input elevation grid
   * @param width The width of the grid
   * @param height The height of the grid
   * @return A BufferedImage representing the elevation data
   */
  public static BufferedImage gridToImage(double[] grid, int width, int height,
      DoubleToIntFunction elevationToPixel) {
    validateGrid(grid, width, height);
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        double elevation = grid[y * width + x];
        image.setRGB(x, y, elevationToPixel.applyAsInt(elevation));
      }
    }

    return image;
  }

  public static double pixelToElevationStandard(int rgb) {
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = rgb & 0xFF;
    return (r * ELEVATION_SCALE + g * 256.0 + b) / 10.0 - ELEVATION_OFFSET;
  }

  public static double pixelToElevationTerrarium(int rgb) {
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = rgb & 0xFF;
    return (r * 256.0 + g + b / 256.0) - 32768.0;
  }

  public static int elevationToPixel(double elevation) {
    int value = (int) ((elevation + ELEVATION_OFFSET) * 10.0);
    int r = (value >> 16) & 0xFF;
    int g = (value >> 8) & 0xFF;
    int b = value & 0xFF;
    return (r << 16) | (g << 8) | b;
  }

  private static void validateImage(BufferedImage image) {
    if (image == null) {
      throw new IllegalArgumentException("Input image cannot be null");
    }
    if (image.getWidth() <= 0 || image.getHeight() <= 0) {
      throw new IllegalArgumentException("Image dimensions must be positive");
    }
  }

  private static void validateGrid(double[] grid, int width, int height) {
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

}
