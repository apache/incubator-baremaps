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
import org.locationtech.jts.geom.GeometryFactory;
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

    private final Map<Integer, Dataset> datasets = new HashMap<>();

    public ContourTileStore() {
    }

    public Dataset initDataset(Integer zoom) {
        try {
            var dem = Files.readString(Paths.get("dem.xml"));
            dem = dem.replace("<TileLevel>0</TileLevel>", "<TileLevel>" + zoom + "</TileLevel>");
            return gdal.Open(dem, gdalconstConstants.GA_ReadOnly);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        var vectorLayer = vectorDataSource.CreateLayer("vector", srs, ogr.wkbLineString);


        String levels = IntStream.range(1, 1000)
                .mapToObj(i -> i * 10)
                .filter(l -> {
                    if (tile.z() <= 9) {
                        return l % 1000 == 0;
                    } else if (tile.z() <= 10) {
                        return l % 800 == 0;
                    } else if (tile.z() <= 11) {
                        return l % 400 == 0;
                    } else if (tile.z() <= 12) {
                        return l % 200 == 0;
                    } else if (tile.z() <= 13) {
                        return l % 100 == 0;
                    } else if (tile.z() <= 14) {
                        return l % 50 == 0;
                    } else {
                        return l % 10 == 0;
                    }
                })
                .map(Object::toString)
                .collect(Collectors.joining(","));

        gdal.ContourGenerateEx(rasterBand, vectorLayer, new Vector<>(List.of("FIXED_LEVELS=" + levels)));

        // return the contours
        var geometries = LongStream.range(0, vectorLayer.GetFeatureCount())
                .mapToObj(vectorLayer::GetFeature)
                .map(feature -> feature.GetGeometryRef())
                .map(geometry -> GeometryUtils.deserialize(geometry.ExportToWkb()))
                .map(targetEnvelope::intersection)
                .toList();

        var features = geometries.stream()
                .map(geometry -> VectorTileFunctions.asVectorTileGeom(geometry, targetEnvelope.getEnvelopeInternal(), 4096, 0,
                        true))
                .filter(geometry -> geometry.getCoordinates().length >= 2)
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
        datasets.values().forEach(Dataset::delete);
    }

    public static void main(String[] args) throws Exception {
        var store = new ContourTileStore();
        store.read(new TileCoord(8492, 5792, 14).parent());
    }

}
