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

import static org.apache.baremaps.dem.HillshadeCalculator.getResolution;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.IntToDoubleFunction;
import org.apache.baremaps.dem.ElevationUtils;
import org.apache.baremaps.dem.HillshadeCalculator;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;

public class RasterHillshadeTileStore implements TileStore<BufferedImage> {

  private final TileStore<BufferedImage> tileStore;

  private final IntToDoubleFunction pixelToElevation;

  public RasterHillshadeTileStore(TileStore<BufferedImage> tileStore,
      IntToDoubleFunction pixelToElevation) {
    this.tileStore = tileStore;
    this.pixelToElevation = pixelToElevation;
  }

  @Override
  public BufferedImage read(TileCoord tileCoord) throws TileStoreException {
    var size = 256;
    var buffer = RasterTileStore.onion(tileStore, tileCoord, 1).getSubimage(
        size - 1,
        size - 1,
        size + 2,
        size + 2);

    var grid = new HillshadeCalculator(
        ElevationUtils.clampGrid(ElevationUtils.imageToGrid(buffer, pixelToElevation), 0, 10000),
        size + 2, size + 2, getResolution(tileCoord.z()))
            .calculate(45, 315);

    // Create an output image
    BufferedImage hillshadeImage =
        new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        int value = (int) grid[(y + 1) * buffer.getHeight() + x + 1];
        hillshadeImage.setRGB(x, y, new Color(value, value, value).getRGB());
      }
    }

    return hillshadeImage;
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
    // Do nothing
  }
}
