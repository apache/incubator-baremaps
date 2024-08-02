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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import org.apache.baremaps.maplibre.vectortile.Feature;
import org.apache.baremaps.maplibre.vectortile.Layer;
import org.apache.baremaps.maplibre.vectortile.Tile;
import org.apache.baremaps.maplibre.vectortile.VectorTileEncoder;
import org.apache.baremaps.raster.ContourTracer;
import org.apache.baremaps.raster.ElevationUtils;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;
import org.locationtech.jts.geom.util.AffineTransformation;

public class ContourTileStore implements TileStore<ByteBuffer> {

  private final TileStore<BufferedImage> tileStore;

  public ContourTileStore(TileStore<BufferedImage> tileStore) {
    this.tileStore = tileStore;
  }

  @Override
  public ByteBuffer read(TileCoord tileCoord) throws TileStoreException {
    var image = tileStore.read(tileCoord);
    var onion = BufferedImageTileStore.onion(tileStore, tileCoord, 1);
    image = onion.getSubimage(
        image.getWidth() - 4,
        image.getHeight() - 4,
        image.getWidth() + 8,
        image.getHeight() + 8);

    var grid = ElevationUtils.imageToGrid(image, ElevationUtils::pixelToElevationTerrarium);
    var features = new ArrayList<Feature>();
    for (int level = -10000; level < 10000; level += 100) {
      var contours = new ContourTracer(grid, image.getWidth(), image.getHeight(), false, false)
          .traceContours(level);
      for (var contour : contours) {
        contour = AffineTransformation
            .translationInstance(-4, -4)
            .scale(16, 16)
            .transform(contour);
        features.add(new Feature(level, Map.of("level", String.valueOf(level)), contour));
      }
    }

    var layer = new Layer("elevation", 4096, features);
    var tile = new Tile(List.of(layer));
    var vectorTile = new VectorTileEncoder().encodeTile(tile);
    try (var baos = new ByteArrayOutputStream()) {
      var gzip = new GZIPOutputStream(baos);
      vectorTile.writeTo(gzip);
      gzip.close();
      return ByteBuffer.wrap(baos.toByteArray());
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
