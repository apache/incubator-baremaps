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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Vector;
import org.gdal.gdal.*;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.*;
import org.gdal.osr.SpatialReference;

public class Main {

  public static void main(String[] args) {
    var sourceFilename = Paths.get("examples/contour/liecthenstein-aster-dem-v2-3857.tif")
        .toAbsolutePath().toString();
    var hillshadeFilename =
        Paths.get("examples/contour/liecthenstein-aster-dem-v2-3857-hillshade.tif").toAbsolutePath()
            .toString();
    var outputFilename = Paths.get("examples/contour/liecthenstein-aster-dem-v2-3857.shp")
        .toAbsolutePath().toString();
    var warpFilename = Paths.get("examples/contour/liecthenstein-aster-dem-v2-3857-warp.tif")
        .toAbsolutePath().toString();

    var dem = Paths.get("examples/contour/dem.xml")
        .toAbsolutePath().toString();

    gdal.AllRegister();
    ogr.RegisterAll();

    planetContour();

    hillshade(sourceFilename, 1, hillshadeFilename, 45d, 315d);
    contourEx(hillshadeFilename, 1, outputFilename, 50, 0);
    warp(sourceFilename, warpFilename);
    shadow(hillshadeFilename, outputFilename);
  }

  public static void planetContour() {
    var file = Paths.get(String.format("%s/%s/%s.tif", 14, 8514, 5816));
    var url = String.format("https://s3.amazonaws.com/elevation-tiles-prod/geotiff/%s", file);
    System.out.println(url);

    try {
      Files.deleteIfExists(file);
      Files.createDirectories(file.getParent());
      Files.createFile(file);
      try (var stream = new URL(url).openStream()) {
        Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
      }
      System.out.println(Files.size(file));
    } catch (Exception e) {
      e.printStackTrace();
    }

    var dataset = gdal.Open(file.toString(), gdalconstConstants.GA_ReadOnly);

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
      System.out.println(geometry.ExportToWkt());
    }

    dataSource.delete();
    dataset.delete();
  }

  public static void contour(String source, Integer sourceBand, String target,
      Integer contourInterval,
      Integer contourBase) {

    var dataset = gdal.Open(source, gdalconstConstants.GA_ReadOnly);
    var band = dataset.GetRasterBand(sourceBand);
    var wkt = dataset.GetProjection();
    var srs = new SpatialReference(wkt);

    var driver = ogr.GetDriverByName("ESRI Shapefile");
    var dataSource = driver.CreateDataSource(target);
    var layer = dataSource.CreateLayer("contour", srs, ogr.wkbLineString);
    var field = new FieldDefn("ID", ogr.OFTInteger);

    field.SetWidth(8);
    layer.CreateField(field, 0);
    field.delete();

    var feature = layer.GetLayerDefn();
    gdal.ContourGenerate(band, contourInterval, contourBase, null,
        0, 0, layer, feature.GetFieldIndex("ID"),
        -1);

    dataSource.delete();
    dataset.delete();
  }

  public static void contourEx(String source, Integer sourceBand, String target,
      Integer contourInterval,
      Integer contourBase) {

    var dataset = gdal.Open(source, gdalconstConstants.GA_ReadOnly);
    var band = dataset.GetRasterBand(sourceBand);
    var wkt = dataset.GetProjection();
    var srs = new SpatialReference(wkt);

    var driver = ogr.GetDriverByName("ESRI Shapefile");
    var dataSource = driver.CreateDataSource(target);
    var layer = dataSource.CreateLayer("contour", srs, ogr.wkbLineString);
    var field = new FieldDefn("ID", ogr.OFTInteger);

    field.SetWidth(8);
    layer.CreateField(field, 0);
    field.delete();

    gdal.ContourGenerateEx(band, layer, new Vector<>(List.of(
        "LEVEL_BASE=" + contourBase,
        "LEVEL_INTERVAL=" + contourInterval,
        "POLYGONIZE=YES")));

    dataSource.delete();
    dataset.delete();
  }

  public static void polygonize(String source, Integer sourceBand, String target) {
    var dataset = gdal.Open(source, gdalconstConstants.GA_ReadOnly);
    var band = dataset.GetRasterBand(sourceBand);
    var wkt = dataset.GetProjection();
    var srs = new SpatialReference(wkt);

    var driver = ogr.GetDriverByName("ESRI Shapefile");
    var dataSource = driver.CreateDataSource(target);
    var layer = dataSource.CreateLayer("polygonize", srs, ogr.wkbPolygon);
    var field = new FieldDefn("ID", ogr.OFTInteger);

    field.SetWidth(8);
    layer.CreateField(field, 0);
    field.delete();

    var feature = layer.GetLayerDefn();
    gdal.Polygonize(band, null, layer, feature.GetFieldIndex("ID"),
        null, null);

    dataSource.delete();
    dataset.delete();
  }

  public static void warp(String source, String target) {
    var dataset = gdal.Open(source, gdalconstConstants.GA_ReadOnly);
    var transform = dataset.GetGeoTransform();
    var xRes = transform[1];
    var yRes = transform[5];

    var options = new Vector<>(List.of(
        "-tr", Double.toString(xRes * 10), Double.toString(yRes * 10)));

    var warp = gdal.Warp(target, new Dataset[] {dataset}, new WarpOptions(options));

    warp.delete();
    dataset.delete();
  }


  public record ShadowClass(int a, int b, int c) {
  }

  private static final List<ShadowClass> shadowClasses = List.of(
      new ShadowClass(1, 250, 255),
      new ShadowClass(2, 240, 255),
      new ShadowClass(3, 1, 150),
      new ShadowClass(4, 1, 100),
      new ShadowClass(5, 1, 65),
      new ShadowClass(6, 1, 2));

  private static void shadow(String source, String target) {
    var dataset = gdal.Open(source, gdalconstConstants.GA_ReadOnly);
    var band = dataset.GetRasterBand(1);
    var wkt = dataset.GetProjection();
    var srs = new SpatialReference(wkt);

    var driver = ogr.GetDriverByName("ESRI Shapefile");
    var dataSource = driver.CreateDataSource(target);

    for (var shadowClass : shadowClasses) {
      var layer = dataSource.CreateLayer("shadow-" + shadowClass.a(), srs, ogr.wkbPolygon);
      var field = new FieldDefn("ID", ogr.OFTInteger);

      field.SetWidth(8);
      layer.CreateField(field, 0);
      field.delete();

      gdal.ContourGenerateEx(band, layer, new Vector<>(List.of(
          "LEVEL_BASE=" + shadowClass.b(),
          "LEVEL_INTERVAL=" + shadowClass.c(),
          "POLYGONIZE=YES")));
    }

    dataSource.delete();
    dataset.delete();
  }

  public static void hillshade(String source, Integer sourceBand, String target, Double azimuth,
      Double altitude) {
    var options = new Vector<>(List.of(
        "-az", azimuth.toString(),
        "-alt", altitude.toString(),
        "-z", "1.0",
        "-s", "1.0",
        "-b", sourceBand.toString(),
        "-of", "GTiff",
        "-combined"));

    var dataset = gdal.Open(source, gdalconstConstants.GA_ReadOnly);
    var hillshadeDataset =
        gdal.DEMProcessing(target, dataset, "hillshade", null, new DEMProcessingOptions(options));

    hillshadeDataset.delete();
    dataset.delete();
  }

}
