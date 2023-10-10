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

package org.apache.baremaps.contour;

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
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.locationtech.proj4j.ProjCoordinate;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OceanContourTileStore implements TileStore, AutoCloseable {

    static {
        // Register the gdal and ogr drivers
        gdal.AllRegister();
        ogr.RegisterAll();
    }

    /**
     * The definition of the digital elevation model.
     */
    private static final String DEM = """
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


    private static final Map<Integer, String> CONTOUR_LEVELS_BY_ZOOM_LEVELS_IN_METERS = IntStream.range(0, 20).mapToObj(zoomLevel -> {
        var contourLevels = IntStream.range(-10000, 1)
                .mapToObj(i -> (double) i)
                .filter(l -> {
                    if (zoomLevel <= 8) {
                        return l % 1000 == 0;
                    } else if (zoomLevel == 9) {
                        return l % 500 == 0;
                    } else if (zoomLevel == 10) {
                        return l % 200 == 0;
                    } else if (zoomLevel == 11) {
                        return l % 100 == 0;
                    } else if (zoomLevel == 12) {
                        return l % 50 == 0;
                    } else if (zoomLevel == 13) {
                        return l % 20 == 0;
                    } else if (zoomLevel == 14) {
                        return l % 10 == 0;
                    } else {
                        return false;
                    }
                })
                .map(Object::toString)
                .collect(Collectors.joining(","));
        return Map.entry(zoomLevel, contourLevels);
    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Map<Integer, String> CONTOUR_LEVELS_BY_ZOOM_LEVELS_IN_FEETS = IntStream.range(0, 20).mapToObj(zoomLevel -> {
        var contourLevels = IntStream.range(-30000, 1)
                .mapToObj(i -> (double) i)
                .filter(l -> {
                    if (zoomLevel <= 9) {
                        return l % 1000 == 0;
                    } else if (zoomLevel == 10) {
                        return l % 500 == 0;
                    } else if (zoomLevel == 11) {
                        return l % 200 == 0;
                    } else if (zoomLevel == 12) {
                        return l % 100 == 0;
                    } else if (zoomLevel == 13) {
                        return l % 50 == 0;
                    } else if (zoomLevel == 14) {
                        return l % 20 == 0;
                    } else {
                        return false;
                    }
                })
                .map(l -> l * 0.3048)
                .map(Object::toString)
                .collect(Collectors.joining(","));
        return Map.entry(zoomLevel, contourLevels);
    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private final Map<Integer, Dataset> datasets = new HashMap<>();

    public OceanContourTileStore() {
    }

    public Dataset initRasterDatasetAtZoomLevel(Integer zoom) {
        if (zoom > 14) {
            zoom = 14;
        }
        var dem = DEM.replace("<TileLevel>0</TileLevel>", "<TileLevel>" + zoom + "</TileLevel>");
        return gdal.Open(dem, gdalconstConstants.GA_ReadOnly);
    }

    @Override
    public ByteBuffer read(TileCoord tile) throws TileStoreException {
        // Transform the tile envelope to the raster projection
        var tileEnvelope = tile.envelope();
        var transformer = GeometryUtils.coordinateTransform(4326, 3857);
        var rasterMin = transformer.transform(
                new ProjCoordinate(tileEnvelope.getMinX(), tileEnvelope.getMinY()),
                new ProjCoordinate());
        var rasterMax = transformer.transform(
                new ProjCoordinate(tileEnvelope.getMaxX(), tileEnvelope.getMaxY()),
                new ProjCoordinate());
        var rasterBuffer = (rasterMax.x - rasterMin.x) / 4096 * 128;
        var rasterEnvelope = new GeometryFactory().createPolygon(new Coordinate[]{
                new Coordinate(rasterMin.x, rasterMin.y),
                new Coordinate(rasterMin.x, rasterMax.y),
                new Coordinate(rasterMax.x, rasterMax.y),
                new Coordinate(rasterMax.x, rasterMin.y),
                new Coordinate(rasterMin.x, rasterMin.y)
        });
        var rasterEnvelopeWithBuffer = rasterEnvelope.buffer(rasterBuffer);

        // Load the raster data for the tile into the memory
        // The cache used by gdal is not thread safe, so we need to synchronize the access to the dataset
        // Otherwise, some tiles would be corrupted
        Dataset rasterDataset1;
        synchronized (this) {
            var dataset = datasets.computeIfAbsent(tile.z(), this::initRasterDatasetAtZoomLevel);
            var rasterOptions1 = new WarpOptions(new Vector<>(List.of(
                    "-of", "MEM",
                    "-te",
                    Double.toString(rasterEnvelopeWithBuffer.getEnvelopeInternal().getMinX()),
                    Double.toString(rasterEnvelopeWithBuffer.getEnvelopeInternal().getMinY()),
                    Double.toString(rasterEnvelopeWithBuffer.getEnvelopeInternal().getMaxX()),
                    Double.toString(rasterEnvelopeWithBuffer.getEnvelopeInternal().getMaxY()),
                    "-te_srs", "EPSG:3857")));
            rasterDataset1 = gdal.Warp("", new Dataset[]{dataset}, rasterOptions1);
        }
        
        // Reduce the resolution of the raster by a factor of 4 to remove artifacts
        var rasterOptions2 = new WarpOptions(new Vector<>(List.of(
                "-of", "MEM",
                "-ts",
                String.valueOf(rasterDataset1.getRasterXSize() / 1),
                String.valueOf(rasterDataset1.getRasterYSize() / 1),
                "-r", "cubicspline")));
        var rasterDataset2 = gdal.Warp("", new Dataset[]{rasterDataset1}, rasterOptions2);

        System.out.println("--------------------");
        System.out.println(rasterDataset1.getRasterXSize());
        System.out.println(rasterDataset1.getRasterYSize());

        var xSize = rasterDataset1.getRasterXSize();
        var ySize = rasterDataset1.getRasterYSize();
        var xFactor = 1088d / xSize;
        var yFactor = 1088d / ySize;

        // Increase the resolution of the raster by a factor of 2 to smooth the contours
        var rasterOptions3 = new WarpOptions(new Vector<>(List.of(
                "-of", "MEM",
                "-ts", String.valueOf(rasterDataset1.getRasterXSize() * xFactor), String.valueOf(rasterDataset1.getRasterYSize() * yFactor),
                "-r", "cubicspline")));
        var rasterDataset3 = gdal.Warp("", new Dataset[]{rasterDataset2}, rasterOptions3);

        // Generate the contours in meters
        var contourLevelsInMeters = CONTOUR_LEVELS_BY_ZOOM_LEVELS_IN_METERS.get(tile.z());
        var featuresInMeters = generateContour(rasterDataset3, rasterEnvelope, rasterEnvelopeWithBuffer, contourLevelsInMeters);

        // Generate the contours in feets
        var contourLevelsInFeets = CONTOUR_LEVELS_BY_ZOOM_LEVELS_IN_FEETS.get(tile.z());
        var featuresInFeets = generateContour(rasterDataset3, rasterEnvelope, rasterEnvelopeWithBuffer, contourLevelsInFeets);

        // Release the resources
        rasterDataset1.delete();
        rasterDataset2.delete();
        rasterDataset3.delete();

        // Create the vector tile
        return VectorTileFunctions
                .asVectorTile(new VectorTile(List.of(
                        new Layer("contour_m", 4096, featuresInMeters),
                        new Layer("contour_ft", 4096, featuresInFeets))));
    }


    public List<Feature> generateContour(Dataset rasterDataset, Geometry rasterEnvelope, Geometry rasterEnvelopeWithBuffer, String contourLevels) {
        // Initialize the vector dataset and layer to store the contours
        var vectorProjection = rasterDataset.GetProjection();
        var vectorSpatialReferenceSystem = new SpatialReference(vectorProjection);
        var vectorMemoryDriver = ogr.GetDriverByName("Memory");
        var vectorDataSource = vectorMemoryDriver.CreateDataSource("vector");
        var vectorLayer = vectorDataSource.CreateLayer("vector", vectorSpatialReferenceSystem, ogr.wkbLineString, new Vector(List.of("ADVERTIZE_UTF8=YES")));
        vectorLayer.CreateField(new FieldDefn("elevation", ogr.OFTReal));

        // Get the raster band to generate the contours from
        var rasterBand = rasterDataset.GetRasterBand(1);

        // Generate the contours and store them in the vector layer
        gdal.ContourGenerateEx(rasterBand, vectorLayer, new Vector<>(List.of("ELEV_FIELD=elevation", "FIXED_LEVELS=" + contourLevels)));

        // return the contours
        var features = new ArrayList<Feature>();
        for (var i = 0; i < vectorLayer.GetFeatureCount(); i++) {
            var vectorFeature = vectorLayer.GetFeature(i);

            // Get the feature id
            var id = vectorFeature.GetFID();

            // Get the feature properties
            var properties = new HashMap<String, Object>();
            for (int j = 0; j < vectorFeature.GetFieldCount(); j++) {
                var field = vectorFeature.GetFieldDefnRef(j);
                var name = field.GetName();
                var value = vectorFeature.GetFieldAsString(name);

                // Parse the elevation
                if (name.equals("elevation")) {
                    var elevationInMeters = Double.parseDouble(value);
                    var elevationInFeet = Math.round(elevationInMeters * 3.28084 * 100) / 100.0;
                    properties.put("elevation_m", elevationInMeters);
                    properties.put("elevation_ft", elevationInFeet);
                }

                // Release the field resources
                field.delete();
            }

            // Get the wkb geometry
            var vectorGeometry = vectorFeature.GetGeometryRef();
            var wkb = vectorGeometry.ExportToWkb();

            // Release the geometry and feature resources
            vectorGeometry.delete();
            vectorFeature.delete();

            // Deserialize the wkb geometry and clip it to the raster envelope with buffer
            var geometry = GeometryUtils.deserialize(wkb);
            geometry = rasterEnvelopeWithBuffer.intersection(geometry);

            // Create the vector tile geometry with the correct buffer
            var mvtGeometry = VectorTileFunctions
                    .asVectorTileGeom(geometry, rasterEnvelope.getEnvelopeInternal(), 4096, 128, true);

            // The following code is used to simplify the geometry at the tile boundaries,
            // which is necessary to prevent artifacts from appearing at the intersections
            // of the vector tiles.

            // Create the vector tile envelope to simplify the geometry at the tile boundaries
            var mvtEnvelope = new GeometryFactory().createPolygon(new Coordinate[]{
                    new Coordinate(0, 0),
                    new Coordinate(0, 4096),
                    new Coordinate(4096, 4096),
                    new Coordinate(4096, 0),
                    new Coordinate(0, 0)
            });

            // The tolerance to use when simplifying the geometry at the tile boundaries.
            // This value is a good balance between preserving the accuracy of the geometry
            // and minimizing the size of the vector tiles.
            var tolerance = 10;

            // Simplify the inside of the vector tile at the tile boundaries
            var insideGeom = mvtGeometry.intersection(mvtEnvelope);
            var insideSimplifier = new TopologyPreservingSimplifier(insideGeom);
            insideSimplifier.setDistanceTolerance(tolerance);
            insideGeom = insideSimplifier.getResultGeometry();

            // Simplify the outside of the vector tile at the tile boundaries
            var outsideGeom = mvtGeometry.difference(mvtEnvelope);
            var outsideSimplifier = new TopologyPreservingSimplifier(outsideGeom);
            outsideSimplifier.setDistanceTolerance(tolerance);
            outsideGeom = outsideSimplifier.getResultGeometry();

            // Merge the simplified geometries back together
            mvtGeometry = insideGeom.union(outsideGeom);

            // Add the feature to the list of features if it is valid and has more than one coordinate
            if (mvtGeometry.isValid() && mvtGeometry.getCoordinates().length > 1) {
                features.add(new Feature(id, properties, mvtGeometry));
            }
        }

        // Release the resources
        vectorLayer.delete();
        rasterBand.delete();

        return features;
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
        var store = new OceanContourTileStore();
        store.read(new TileCoord(8492, 5792, 14).parent());
    }
}
