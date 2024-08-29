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
import java.util.function.IntToDoubleFunction;
import java.util.zip.GZIPOutputStream;
import org.apache.baremaps.dem.ContourTracer;
import org.apache.baremaps.dem.ElevationUtils;
import org.apache.baremaps.dem.HillshadeCalculator;
import org.apache.baremaps.maplibre.vectortile.Feature;
import org.apache.baremaps.maplibre.vectortile.Layer;
import org.apache.baremaps.maplibre.vectortile.Tile;
import org.apache.baremaps.maplibre.vectortile.VectorTileEncoder;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

public class VectorHillshadeTileStore implements TileStore<ByteBuffer> {

  private final GeoTiffReader geoTiffReader;

  private final IntToDoubleFunction pixelToElevation;

  public VectorHillshadeTileStore(GeoTiffReader geoTiffReader,
      IntToDoubleFunction pixelToElevation) {
    this.geoTiffReader = geoTiffReader;
    this.pixelToElevation = pixelToElevation;
  }

  @Override
  public ByteBuffer read(TileCoord tileCoord) throws TileStoreException {
    try {
      var size = 256;

      // Read the elevation data
      var features = new ArrayList<Feature>();

      // Calculate the hillshade
      var grid = geoTiffReader.read(tileCoord, size, 16);
      grid = ElevationUtils.clampGrid(grid, 0, 10000);
      grid = new HillshadeCalculator(
          grid,
          size + 32, size + 32, HillshadeCalculator.getResolution(tileCoord.z()) / 2)
              .calculate(45, 315);

      contours(grid, 255 - 16, features, "1");
      contours(grid, 255 - 32, features, "2");

      grid = ElevationUtils.invertGrid(grid);
      contours(grid, 255 - 32, features, "6");
      contours(grid, 255 - 64, features, "5");
      contours(grid, 255 - 98, features, "4");
      contours(grid, 255 - 128, features, "3");

      var layer = new Layer("elevation", 4096, features);
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

  private static void contours(double[] grid, int level, ArrayList<Feature> features, String id) {
    var contours =
        new ContourTracer(grid, (int) Math.sqrt(grid.length), (int) Math.sqrt(grid.length), false,
            true)
                .traceContours(level);
    for (var contour : contours) {
      contour = AffineTransformation
          .translationInstance(-16, -16)
          .scale(16, 16)
          .transform(contour);

      contour = TopologyPreservingSimplifier.simplify(contour, 4);
      features.add(new Feature(4, Map.of("level", id), contour));
    }
  }

  /** Unsupported operation. */
  @Override
  public void write(TileCoord tileCoord, ByteBuffer blob) throws TileStoreException {
    throw new UnsupportedOperationException();
  }

  /** Unsupported operation. */
  @Override
  public void delete(TileCoord tileCoord) throws TileStoreException {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  @Override
  public void close() throws Exception {
    // Do nothing
  }
}
