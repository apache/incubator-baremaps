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

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;
import org.apache.baremaps.database.tile.Tile;
import org.apache.baremaps.database.tile.TileStore;
import org.apache.baremaps.database.tile.TileStoreException;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;

public class ContourTileStore implements TileStore {
  @Override
  public ByteBuffer read(Tile tile) throws TileStoreException {
    var file = Paths.get(String.format("%s/%s/%s.tif", tile.z(), tile.x(), tile.y()));
    var source = String.format("https://s3.amazonaws.com/elevation-tiles-prod/geotiff/%s", file);

    try {
      Files.deleteIfExists(file);
      Files.createDirectories(file.getParent());
      Files.createFile(file);
      try (var stream = new URL(source).openStream()) {
        Files.copy(stream, file);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    var dataset = gdal.Open(file.toString(), gdalconstConstants.GA_ReadOnly);

    dataset.GetGeoTransform();
    dataset.GetRasterXSize();
    dataset.GetRasterYSize();


    var band = dataset.GetRasterBand(1);

    band.ReadRaster_Direct(0, 0, 100, 100);

    var wkt = dataset.GetProjection();
    var srs = new SpatialReference(wkt);

    var driver = ogr.GetDriverByName("Memory");
    var dataSource = driver.CreateDataSource("memory_name");

    var layer = dataSource.CreateLayer("contour", srs, ogr.wkbLineString);

    var field = new FieldDefn("ID", ogr.OFTInteger);
    field.SetWidth(8);
    layer.CreateField(field, 0);
    field.delete();

    gdal.ContourGenerateEx(band, layer, new Vector<>(List.of(
        "LEVEL_INTERVAL=" + 10)));

    for (int i = 0; i < layer.GetFeatureCount(); i++) {
      var feature = layer.GetFeature(i);
      var geometry = feature.GetGeometryRef();
      // System.out.println(geometry.ExportToWkt());
    }

    dataSource.delete();
    dataset.delete();
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
}
