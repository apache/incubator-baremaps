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

package org.apache.baremaps.tilestore.raster;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.baremaps.raster.ElevationUtils;
import org.apache.baremaps.gdal.Gdal;
import org.apache.baremaps.gdal.WarpOptions;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;

public class RasterElevationTileStore implements TileStore<BufferedImage> {

  private final Path path;

  public RasterElevationTileStore() {
    Gdal.initialize();
    path = Paths.get("/data/gebco_2024_web_mercator.tif");
  }

  @Override
  public BufferedImage read(TileCoord tileCoord) throws TileStoreException {
    var envelope = tileCoord.envelope();

    var options = new WarpOptions()
        .outputFormat("MEM")
        .targetExtent(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(),
            envelope.getMaxY())
        .targetExtentSRS("EPSG:4326")
        .targetSize(256, 256)
        .resampling("cubicspline")
        .errorThreshold(10);

    try (var sourceDataset = Gdal.open(path);
        var targetDataset = Gdal.warp(sourceDataset, options);
        var targetBand = targetDataset.getRasterBand(1)) {

      // Copy the data of the band into a byte array
      double[] values = new double[targetBand.getWidth() * targetBand.getHeight()];
      targetBand.read(0, 0, 256, 256, values);

      // Create a BufferedImage from the byte array
      BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);

      for (int x = 0; x < 256; x++) {
        for (int y = 0; y < 256; y++) {
          double value = values[y * 256 + x];
          int pixel = ElevationUtils.elevationToPixelTerrarium(value);
          image.setRGB(x, y, pixel);
        }
      }

      return image;

    } catch (Exception e) {
      throw new TileStoreException(e);
    }
  }

  @Override
  public void write(TileCoord tileCoord, BufferedImage blob) throws TileStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(TileCoord tileCoord) throws TileStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws Exception {
    throw new UnsupportedOperationException();
  }

  public static void main(String... args) throws TileStoreException {
    new RasterElevationTileStore().read(new TileCoord(8511, 5821, 14));
  }
}
