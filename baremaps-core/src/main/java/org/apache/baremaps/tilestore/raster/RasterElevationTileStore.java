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
import java.util.Vector;
import org.apache.baremaps.openstreetmap.utils.GeometryUtils;
import org.apache.baremaps.raster.ElevationUtils;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.WarpOptions;
import org.gdal.gdal.gdal;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.proj4j.ProjCoordinate;

public class RasterElevationTileStore implements TileStore<BufferedImage> {

  private final Path path;

  public RasterElevationTileStore() {
    gdal.AllRegister();
    path = Paths.get("/data/gebco_2024_web_mercator.tif");
  }

  @Override
  public BufferedImage read(TileCoord tileCoord) throws TileStoreException {
    Dataset sourceDataset = null;
    Dataset targetDataset = null;
    Band targetBand = null;

    try {
      sourceDataset = gdal.Open(path.toString());

      var envelope = tileCoord.envelope();
      var transformer = GeometryUtils.coordinateTransform(4326, 3857);
      var rasterMin = transformer.transform(
          new ProjCoordinate(envelope.getMinX(), envelope.getMinY()),
          new ProjCoordinate());
      var rasterMax = transformer.transform(
          new ProjCoordinate(envelope.getMaxX(), envelope.getMaxY()),
          new ProjCoordinate());
      var rasterEnvelope = new GeometryFactory().createPolygon(new Coordinate[] {
          new Coordinate(rasterMin.x, rasterMin.y),
          new Coordinate(rasterMin.x, rasterMax.y),
          new Coordinate(rasterMax.x, rasterMax.y),
          new Coordinate(rasterMax.x, rasterMin.y),
          new Coordinate(rasterMin.x, rasterMin.y)
      }).getEnvelopeInternal();

      Vector<String> rasterOptions = new Vector<>();
      rasterOptions.add("-of");
      rasterOptions.add("MEM");

      rasterOptions.add("-te");
      rasterOptions.add(String.valueOf(rasterEnvelope.getMinX()));
      rasterOptions.add(String.valueOf(rasterEnvelope.getMinY()));
      rasterOptions.add(String.valueOf(rasterEnvelope.getMaxX()));
      rasterOptions.add(String.valueOf(rasterEnvelope.getMaxY()));

      rasterOptions.add("-ts");
      rasterOptions.add(String.valueOf(256));
      rasterOptions.add(String.valueOf(256));

      rasterOptions.add("-r");
      rasterOptions.add("cubicspline");

      rasterOptions.add("-et");
      rasterOptions.add("10");

      targetDataset = gdal.Warp("", new Dataset[] {sourceDataset}, new WarpOptions(rasterOptions));
      targetBand = targetDataset.GetRasterBand(1);

      // Copy the data of the band into a byte array
      double[] values = new double[256 * 256];
      targetBand.ReadRaster(0, 0, 256, 256, values);

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

    } finally {
      if (sourceDataset != null) {
        sourceDataset.delete();
      }
      if (targetDataset != null) {
        targetDataset.delete();
      }
      if (targetBand != null) {
        targetBand.delete();
      }
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
    gdal.AllRegister();
    new RasterElevationTileStore().read(new TileCoord(8511, 5821, 14));
  }
}
