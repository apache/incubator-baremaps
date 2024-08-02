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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.IntToDoubleFunction;
import javax.imageio.ImageIO;
import org.apache.baremaps.raster.ElevationUtils;
import org.apache.baremaps.raster.HillshadeCalculator;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;

public class HillshadeTileStore implements TileStore<ByteBuffer> {

  private final TileStore<BufferedImage> tileStore;

  private final IntToDoubleFunction pixelToElevation;

  public HillshadeTileStore(TileStore<BufferedImage> tileStore,
      IntToDoubleFunction pixelToElevation) {
    this.tileStore = tileStore;
    this.pixelToElevation = pixelToElevation;
  }

  @Override
  public ByteBuffer read(TileCoord tileCoord) throws TileStoreException {
    try {
      var image = tileStore.read(tileCoord);
      var onion = BufferedImageTileStore.onion(tileStore, tileCoord, 1);
      var buffer = onion.getSubimage(
          image.getWidth() - 1,
          image.getHeight() - 1,
          image.getWidth() + 2,
          image.getHeight() + 2);

      var grid = ElevationUtils.imageToGrid(buffer, pixelToElevation);
      var hillshadeGrid =
          new HillshadeCalculator(grid, buffer.getWidth(), buffer.getHeight(), 1, false)
              .calculate(45, 315);

      // Create an output image
      BufferedImage hillshadeImage =
          new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
      for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < image.getWidth(); x++) {
          int value = (int) hillshadeGrid[(y + 1) * buffer.getHeight() + x + 1];
          hillshadeImage.setRGB(x, y, new Color(value, value, value).getRGB());
        }
      }

      try (var baos = new ByteArrayOutputStream()) {
        ImageIO.write(hillshadeImage, "png", baos);
        baos.flush();
        return ByteBuffer.wrap(baos.toByteArray());
      }
    } catch (IOException e) {
      throw new TileStoreException(e);
    }
  }

  @Override
  public void write(TileCoord tileCoord, ByteBuffer blob) throws TileStoreException {
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
