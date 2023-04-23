/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.raster;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.apache.baremaps.database.tile.Tile;
import org.apache.baremaps.database.tile.TileStore;
import org.apache.baremaps.database.tile.TileStoreException;
import org.apache.baremaps.openstreetmap.utils.GeometryUtils;
import org.apache.baremaps.openstreetmap.utils.ProjectionTransformer;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.WarpOptions;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.proj4j.ProjCoordinate;

public class ContourTileStore implements TileStore, AutoCloseable {

  static {
    gdal.AllRegister();
    ogr.RegisterAll();
  }

  private final Dataset sourceDataset;

  public ContourTileStore() {
    var dem = Paths.get("examples/contour/dem.xml").toAbsolutePath().toString();
    sourceDataset = gdal.Open(dem, gdalconstConstants.GA_ReadOnly);
  }

  @Override
  public ByteBuffer read(Tile tile) throws TileStoreException {
    var sourceBand = sourceDataset.GetRasterBand(1);
    var envelope = tile.envelope();

    // Warp the raster to the requested extent
    var rasterOptions = new WarpOptions(new Vector<>(List.of(
            "-of", "MEM",
            "-te", Double.toString(envelope.getMinX()), Double.toString(envelope.getMinY()), Double.toString(envelope.getMaxX()), Double.toString(envelope.getMaxY()),
            "-te_srs", "EPSG:4326")));
    var rasterDataset = gdal.Warp("", new Dataset[]{sourceDataset}, rasterOptions);
    var rasterBand = rasterDataset.GetRasterBand(1);

    // Generate the contours
    //var wkt = rasterDataset.GetProjection();
    //var srs = new SpatialReference(wkt);
    var srs = new SpatialReference("EPSG:4326");
    var vectorDriver = ogr.GetDriverByName("Memory");
    var vectorDataSource = vectorDriver.CreateDataSource("vector");
    var vectorLayer = vectorDataSource.CreateLayer("vector", srs, ogr.wkbLineString);
    gdal.ContourGenerateEx(rasterBand, vectorLayer, new Vector<>(List.of("LEVEL_INTERVAL=" + 10)));

    // return the contours
    var geometries = LongStream.range(0, vectorLayer.GetFeatureCount())
            .mapToObj(vectorLayer::GetFeature)
            .map(feature -> feature.GetGeometryRef())
            .map(geometry -> GeometryUtils.deserialize(geometry.ExportToWkb()))
            .toList();

    var transformer = GeometryUtils.coordinateTransform(4326, 3857);
    var min = transformer.transform(new ProjCoordinate(envelope.getMinX(), envelope.getMinY()), new ProjCoordinate());
    var max = transformer.transform(new ProjCoordinate(envelope.getMaxX(), envelope.getMaxY()), new ProjCoordinate());

    rasterBand.delete();
    rasterDataset.delete();
    sourceBand.delete();

    return null;
  }

  @Override
  public void write(Tile tile, ByteBuffer blob) throws TileStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Tile tile) throws TileStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws Exception {
    sourceDataset.delete();
  }

  public static void main(String[] args) throws Exception {
    var store = new ContourTileStore();
    store.read(new Tile(8492, 5792, 14).parent());
  }


}
