package io.gazetteer.tileserver;

import io.gazetteer.mbtiles.MBTilesTile;
import io.gazetteer.tiles.Coordinate;
import mil.nga.sf.GeometryEnvelope;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class PGTilesDataSource implements TileDataSource {

    public static final String MIME_TYPE = "application/vnd.mapbox-vector-tile";

    public static final String SELECT_MVT =
            "SELECT ST_AsMVT(q, 'internal-layer-name', 4096, 'geom')" +
            "FROM (" +
            "SELECT ST_AsMVTGeom(" +
            "geom," +
            "ST_MakeEnvelope(%f, %f, %f, %f, 4326)," +
            "4096," +
            "256," +
            "false" +
            ") geom FROM ways c" +
            ") q";
    
    
    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public CompletableFuture<MBTilesTile> getTile(Coordinate coordinate) {

        try {
            try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/osm?user=osm&password=osm")) {
                /*
                PreparedStatement statement = connection.prepareStatement(SELECT_MVT);

                statement.setDouble(1, envelope.getMinX());
                statement.setDouble(2, envelope.getMinY());
                statement.setDouble(3, envelope.getMaxX());
                statement.setDouble(4, envelope.getMaxY());
                //ResultSet result = statement.executeQuery();
                */
                GeometryEnvelope envelope = coordinate.envelope();

                System.out.println(String.format(SELECT_MVT, envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY()));

                return CompletableFuture.completedFuture(new MBTilesTile(null));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
