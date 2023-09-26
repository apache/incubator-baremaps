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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.locationtech.proj4j.ProjCoordinate;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class ContourTileStore implements TileStore, AutoCloseable {

    static {
        gdal.AllRegister();
        ogr.RegisterAll();
    }

    private final String dem = """
            <GDAL_WMS>
                <Service name="TMS">
                    <ServerUrl>https://s3.amazonaws.com/elevation-tiles-prod/geotiff/${z}/${x}/${y}.tif</ServerUrl>
                </Service>
                <DataWindow>
                    <UpperLeftX>-20037508.34</UpperLeftX>
                    <UpperLeftY>20037508.34</UpperLeftY>
                    <LowerRightX>20037508.34</LowerRightX>
                    <LowerRightY>-20037508.34</LowerRightY>
                    <TileLevel>0</TileLevel>
                    <TileCountX>1</TileCountX>
                    <TileCountY>1</TileCountY>
                    <YOrigin>top</YOrigin>
                </DataWindow>
                <Projection>EPSG:3857</Projection>
                <BlockSizeX>512</BlockSizeX>
                <BlockSizeY>512</BlockSizeY>
                <BandsCount>1</BandsCount>
                <DataType>Int16</DataType>
                <ZeroBlockHttpCodes>403,404</ZeroBlockHttpCodes>
                <DataValues>
                    <NoData>-32768</NoData>
                </DataValues>
                <Cache/>
            </GDAL_WMS>
            """;

    private final Map<Integer, Dataset> datasets = new HashMap<>();

    public ContourTileStore() {
    }

    public Dataset initDataset(Integer zoom) {
        var dem = this.dem.replace("<TileLevel>0</TileLevel>", "<TileLevel>" + zoom + "</TileLevel>");
        return gdal.Open(dem, gdalconstConstants.GA_ReadOnly);
    }


    @Override
    public synchronized ByteBuffer read(TileCoord tile) throws TileStoreException {
        var dataset = datasets.computeIfAbsent(tile.z(), this::initDataset);
        var sourceBand = dataset.GetRasterBand(1);
        var envelope = tile.envelope();

        // Transform the extent to the source projection
        var transformer = GeometryUtils.coordinateTransform(4326, 3857);
        var min = transformer.transform(new ProjCoordinate(envelope.getMinX(), envelope.getMinY()),
                new ProjCoordinate());
        var max = transformer.transform(new ProjCoordinate(envelope.getMaxX(), envelope.getMaxY()),
                new ProjCoordinate());
        var buffer = (max.x - min.x) / 4096 * 16;

        var targetEnvelope = new GeometryFactory().createPolygon(new Coordinate[]{
                new Coordinate(min.x, min.y),
                new Coordinate(min.x, max.y),
                new Coordinate(max.x, max.y),
                new Coordinate(max.x, min.y),
                new Coordinate(min.x, min.y)
        });
        var bufferedEnvelope = targetEnvelope.buffer(buffer);

        // Warp the raster to the requested extent
        var rasterOptions = new WarpOptions(new Vector<>(List.of(
                "-of", "MEM",
                "-te", Double.toString(bufferedEnvelope.getEnvelopeInternal().getMinX()), Double.toString(bufferedEnvelope.getEnvelopeInternal().getMinY()),
                Double.toString(bufferedEnvelope.getEnvelopeInternal().getMaxX()), Double.toString(bufferedEnvelope.getEnvelopeInternal().getMaxY()),
                "-te_srs", "EPSG:3857")));
        var rasterDataset = gdal.Warp("", new Dataset[]{dataset}, rasterOptions);
        var rasterBand = rasterDataset.GetRasterBand(1);

        // Generate the contours
        var wkt = rasterDataset.GetProjection();
        var srs = new SpatialReference(wkt);
        var vectorDriver = ogr.GetDriverByName("Memory");
        var vectorDataSource = vectorDriver.CreateDataSource("vector");
        var vectorLayer = vectorDataSource.CreateLayer("vector", srs, ogr.wkbLineString, new Vector(List.of("ADVERTIZE_UTF8=YES")));

        vectorLayer.CreateField(new org.gdal.ogr.FieldDefn("elevation", ogr.OFTReal));

        String levels = IntStream.range(1, 1000)
                .mapToObj(i -> i * 10)
                .filter(l -> {
                    if (tile.z() <= 4) {
                        return l == 1000 || l == 3000 || l == 5000 || l == 7000 || l == 9000;
                    } else if (tile.z() <= 8) {
                        return l % 800 == 0;
                    } else if (tile.z() == 9) {
                        return l % 400 == 0;
                    } else if (tile.z() == 10) {
                        return l % 400 == 0;
                    } else if (tile.z() == 11) {
                        return l % 200 == 0;
                    } else if (tile.z() == 12) {
                        return l % 200 == 0;
                    } else if (tile.z() == 13) {
                        return l % 100 == 0;
                    } else if (tile.z() == 14) {
                        return l % 50 == 0;
                    } else {
                        return false;
                    }
                })
                .map(Object::toString)
                .collect(Collectors.joining(","));

        gdal.ContourGenerateEx(rasterBand, vectorLayer, new Vector<>(List.of("ELEV_FIELD=elevation", "FIXED_LEVELS=" + levels)));

        // return the contours
        var featureCount = vectorLayer.GetFeatureCount();
        var features = LongStream.range(0, featureCount).mapToObj(featureIndex -> {
                    var feature = vectorLayer.GetFeature(featureIndex);
                    var id = feature.GetFID();
                    var properties = new HashMap<String, Object>();
                    var fieldCount = feature.GetFieldCount();
                    for (int i = 0; i < fieldCount; i++) {
                        var field = feature.GetFieldDefnRef(i);
                        var name = field.GetName();
                        var value = feature.GetFieldAsString(name);
                        properties.put(name, value);
                        field.delete();
                    }
                    var ref = feature.GetGeometryRef();
                    var wkb = ref.ExportToWkb();
                    var geometry = GeometryUtils.deserialize(wkb);
                    var tileGeometry = targetEnvelope.intersection(geometry);
                    var mvtGeometry = VectorTileFunctions
                            .asVectorTileGeom(tileGeometry, targetEnvelope.getEnvelopeInternal(), 4096, 0, true);

                    feature.delete();
                    return new Feature(id, properties, mvtGeometry);
                })
                .filter(feature -> feature.getGeometry().getCoordinates().length >= 2)
                .toList();

        var vectorTile = VectorTileFunctions
                .asVectorTile(new VectorTile(List.of(new Layer("contours", 4096, features))));

        vectorLayer.delete();
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
        datasets.values().forEach(Dataset::delete);
    }

    public static void main(String[] args) throws Exception {
        var store = new ContourTileStore();
        store.read(new TileCoord(8492, 5792, 14).parent());
    }

}
