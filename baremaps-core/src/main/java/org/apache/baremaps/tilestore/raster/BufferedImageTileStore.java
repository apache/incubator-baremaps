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
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;

public class BufferedImageTileStore implements TileStore<BufferedImage> {

  private String url;

  public BufferedImageTileStore(String url) {
    this.url = url;
  }

  @Override
  public BufferedImage read(TileCoord tileCoord) throws TileStoreException {
    try {
      var tileUrl = new URL(this.url
          .replace("{z}", String.valueOf(tileCoord.z()))
          .replace("{x}", String.valueOf(tileCoord.x()))
          .replace("{y}", String.valueOf(tileCoord.y())));
      return ImageIO.read(tileUrl);
    } catch (IOException e) {
      throw new TileStoreException(e);
    }
  }

  @Override
  public void write(TileCoord tileCoord, BufferedImage blob) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(TileCoord tileCoord) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws Exception {
    // Do nothing
  }

  public static BufferedImage onion(TileStore<BufferedImage> tileStore, TileCoord centerTile,
      int layers) throws TileStoreException {
    BufferedImage centerImage = tileStore.read(centerTile);
    int mosaicWidth = centerImage.getWidth() * (2 * layers + 1);
    int mosaicHeight = centerImage.getHeight() * (2 * layers + 1);
    BufferedImage onionImage = new BufferedImage(mosaicWidth, mosaicHeight, centerImage.getType());
    for (int x = centerTile.x() - layers; x <= centerTile.x() + layers; x++) {
      for (int y = centerTile.y() - layers; y <= centerTile.y() + layers; y++) {
        TileCoord tileCoord = new TileCoord(x, y, centerTile.z());
        BufferedImage tileImage;
        if (x == centerTile.x() && y == centerTile.y()) {
          tileImage = centerImage;
        } else {
          tileImage = tileStore.read(tileCoord);
        }
        for (int i = 0; i < tileImage.getWidth(); i++) {
          for (int j = 0; j < tileImage.getHeight(); j++) {
            onionImage.setRGB(
                (x - centerTile.x() + layers) * centerImage.getWidth() + i,
                (y - centerTile.y() + layers) * centerImage.getHeight() + j,
                tileImage.getRGB(i, j));
          }
        }
      }
    }
    return onionImage;
  }
}
