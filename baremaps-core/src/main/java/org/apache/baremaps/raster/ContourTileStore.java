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
import java.util.Map;
import java.util.Vector;
import java.util.stream.LongStream;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;
import org.apache.baremaps.utils.GeometryUtils;
import org.apache.baremaps.vectortile.Feature;
import org.apache.baremaps.vectortile.Layer;
import org.apache.baremaps.vectortile.VectorTile;
import org.apache.baremaps.vectortile.VectorTileFunctions;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.WarpOptions;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.proj4j.ProjCoordinate;

public class ContourTileStore implements TileStore, AutoCloseable {

  static {
    gdal.AllRegister();
    ogr.RegisterAll();
  }

  private final Dataset sourceDataset;

  public ContourTileStore() {
    var dem = Paths.get("dem.xml").toAbsolutePath().toString();
    sourceDataset = gdal.Open(dem, gdalconstConstants.GA_ReadOnly);
  }

  @Override
  public ByteBuffer read(TileCoord tile) throws TileStoreException {
    var sourceBand = sourceDataset.GetRasterBand(1);
    var envelope = tile.envelope();

    // Transform the extent to the source projection
    var transformer = GeometryUtils.coordinateTransform(4326, 3857);
    var min = transformer.transform(new ProjCoordinate(envelope.getMinX(), envelope.getMinY()),
        new ProjCoordinate());
    var max = transformer.transform(new ProjCoordinate(envelope.getMaxX(), envelope.getMaxY()),
        new ProjCoordinate());
    var targetEnvelope = new Envelope(min.x, max.x, min.y, max.y);

    // Warp the raster to the requested extent
    var rasterOptions = new WarpOptions(new Vector<>(List.of(
        "-of", "MEM",
        "-te", Double.toString(envelope.getMinX()), Double.toString(envelope.getMinY()),
        Double.toString(envelope.getMaxX()), Double.toString(envelope.getMaxY()),
        "-te_srs", "EPSG:4326")));
    var rasterDataset = gdal.Warp("", new Dataset[] {sourceDataset}, rasterOptions);
    var rasterBand = rasterDataset.GetRasterBand(1);

    // Generate the contours
    var wkt = rasterDataset.GetProjection();
    var srs = new SpatialReference(wkt);
    var vectorDriver = ogr.GetDriverByName("Memory");
    var vectorDataSource = vectorDriver.CreateDataSource("vector");
    var vectorLayer = vectorDataSource.CreateLayer("vector", srs, ogr.wkbLineString);
    gdal.ContourGenerateEx(rasterBand, vectorLayer, new Vector<>(List.of("LEVEL_INTERVAL=" + 50)));

    // return the contours
    var features = LongStream.range(0, vectorLayer.GetFeatureCount())
        .mapToObj(vectorLayer::GetFeature)
        .map(feature -> feature.GetGeometryRef())
        .map(geometry -> GeometryUtils.deserialize(geometry.ExportToWkb()))
        .map(geometry -> VectorTileFunctions.asVectorTileGeom(geometry, targetEnvelope, 4096, 0,
            true))
        .map(geometry -> new Feature(null, Map.of(), geometry))
        .toList();

    var vectorTile = VectorTileFunctions
        .asVectorTile(new VectorTile(List.of(new Layer("contours", 4096, features))));

    rasterBand.delete();
    rasterDataset.delete();
    sourceBand.delete();

    return vectorTile;
  }

  @Override
  public void write(TileCoord tile, ByteBuffer blob) throws TileStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(TileCoord tile) throws TileStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws Exception {
    sourceDataset.delete();
  }

  public static void main(String[] args) throws Exception {
    var store = new ContourTileStore();
    store.read(new TileCoord(8492, 5792, 14).parent());
  }

}
