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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import org.apache.baremaps.dem.ContourTracer;
import org.apache.baremaps.maplibre.vectortile.Feature;
import org.apache.baremaps.maplibre.vectortile.Layer;
import org.apache.baremaps.maplibre.vectortile.Tile;
import org.apache.baremaps.maplibre.vectortile.VectorTileEncoder;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;
import org.locationtech.jts.geom.util.AffineTransformation;

/**
 * A {@code TileStore} that calculates vector contour tiles from elevation tiles.
 */
public class VectorContourTileStore implements TileStore<ByteBuffer> {

  private final GeoTiffReader geoTiffReader;

  /**
   * Constructs a {@code VectorContourTileStore} with the specified GeoTIFF reader.
   *
   * @param geoTiffReader the geotiff reader
   */
  public VectorContourTileStore(GeoTiffReader geoTiffReader) {
    this.geoTiffReader = geoTiffReader;
  }

  /**
   * Read the contour data for the specified tile coordinate.
   *
   * @param tileCoord the tile coordinate
   * @return the contour data
   * @throws TileStoreException if an error occurs
   */
  @Override
  public ByteBuffer read(TileCoord tileCoord) throws TileStoreException {
    try {
      var grid = geoTiffReader.read(tileCoord, 256, 4);

      int increment = switch (tileCoord.z()) {
        case 1 -> 2000;
        case 2 -> 2000;
        case 3 -> 1000;
        case 4 -> 1000;
        case 5 -> 1000;
        case 6 -> 1000;
        case 7 -> 1000;
        case 8 -> 500;
        case 9 -> 500;
        case 10 -> 250;
        case 11 -> 250;
        case 12 -> 100;
        case 13 -> 100;
        case 14 -> 50;
        default -> 10;
      };

      var features = new ArrayList<Feature>();
      for (int level = -10000; level < 10000; level += increment) {
        var contours =
            new ContourTracer(grid, 264, 264, false, true)
                .traceContours(level);
        for (var contour : contours) {
          contour = AffineTransformation
              .translationInstance(-4, -4)
              .scale(16, 16)
              .transform(contour);
          features.add(new Feature(level, Map.of("level", String.valueOf(level)), contour));
        }
      }

      var layer = new Layer("contour", 4096, features);
      var tile = new Tile(List.of(layer));
      var vectorTile = new VectorTileEncoder().encodeTile(tile);
      try (var baos = new ByteArrayOutputStream()) {
        var gzip = new GZIPOutputStream(baos);
        vectorTile.writeTo(gzip);
        gzip.close();
        return ByteBuffer.wrap(baos.toByteArray());
      }
    } catch (Exception e) {
      throw new TileStoreException(e);
    }
  }

  /**
   * Unsupported operation.
   */
  @Override
  public void write(TileCoord tileCoord, ByteBuffer blob) throws TileStoreException {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported operation.
   */
  @Override
  public void delete(TileCoord tileCoord) throws TileStoreException {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  @Override
  public void close() throws Exception {
    geoTiffReader.close();
  }
}
