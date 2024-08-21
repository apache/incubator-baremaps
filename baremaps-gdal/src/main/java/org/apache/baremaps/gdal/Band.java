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

package org.apache.baremaps.gdal;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Band implements AutoCloseable {

  private final org.gdal.gdal.Band band;

  protected Band(org.gdal.gdal.Band band) {
    this.band = band;
  }

  public int getWidth() {
    return band.getXSize();
  }

  public int getHeight() {
    return band.getYSize();
  }

  public void read(int x, int y, int width, int height, byte[] buffer) {
    band.ReadRaster(x, y, width, height, buffer);
  }

  public void read(int x, int y, int width, int height, int[] buffer) {
    band.ReadRaster(x, y, width, height, buffer);
  }

  public void read(int x, int y, int width, int height, long[] buffer) {
    band.ReadRaster(x, y, width, height, buffer);
  }

  public void read(int x, int y, int width, int height, double[] buffer) {
    band.ReadRaster(x, y, width, height, buffer);
  }

  public BufferedImage asBufferedImage(int imageType) {
    // Copy the data of the band into a byte array
    int width = band.getXSize();
    int height = band.getYSize();
    double[] values = new double[height * width];
    band.ReadRaster(0, 0, 256, 256, values);

    // Create a BufferedImage from the byte array
    BufferedImage image = new BufferedImage(width, height, imageType);
    WritableRaster raster = image.getRaster();
    for (int x = 0; x < 256; x++) {
      for (int y = 0; y < 256; y++) {
        double value = values[y * 256 + x];
        raster.setDataElements(x, y, value);
      }
    }

    return image;
  }

  @Override
  public void close() throws Exception {
    band.delete();
  }

}
