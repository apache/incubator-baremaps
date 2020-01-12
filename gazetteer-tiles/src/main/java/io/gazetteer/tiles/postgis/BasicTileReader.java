package io.gazetteer.tiles.postgis;

import io.gazetteer.tiles.Tile;
import io.gazetteer.tiles.TileException;
import io.gazetteer.tiles.TileReader;
import io.gazetteer.tiles.config.Config;
import io.gazetteer.tiles.config.Layer;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.dbcp2.PoolingDataSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

public class BasicTileReader extends AbstractTileReader {

    // {0} = values; {1} = sources
    private static final String SQL_LAYERS = "SELECT {0} FROM {1}";

    // {0} = name;
    private static final String SQL_VALUE = "ST_AsMVT(mvt_geom, ''{0}'', 4096, ''geom'')";

    // {0} = name; {1} = sql; {2} = envelope
    private static final String SQL_SOURCE =
            "(SELECT id, "
                    + "(tags || hstore(''geometry'', lower(replace(st_geometrytype(geom), ''ST_'', ''''))))::jsonb, "
                    + "ST_AsMvtGeom(geom, {2}, 4096, 256, true) AS geom "
                    + "FROM ({1}) AS layer "
                    + "WHERE ST_Intersects(geom, {2})"
                    + ") as mvt_geom";

    private static final CharSequence SQL_UNION_ALL = " UNION ALL ";

    private final PoolingDataSource datasource;

    private final Config config;

    public BasicTileReader(PoolingDataSource datasource, Config config) {
        this.datasource = datasource;
        this.config = config;
    }

    @Override
    public byte[] read(Tile tile) throws TileException {
        try (Connection connection = datasource.getConnection();
             ByteArrayOutputStream data = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(data)) {
            for (Layer layer : config.getLayers()) {
                if (tile.getZ() >= layer.getMinZoom() && tile.getZ() <= layer.getMaxZoom()) {
                    String value = MessageFormat.format(SQL_VALUE, layer.getName());
                    String source = MessageFormat.format(SQL_SOURCE,
                            layer.getName(),
                            layer.getQueries().stream().map(query -> query.getSql()).collect(Collectors.joining(SQL_UNION_ALL)),
                            envelope(tile));
                    String sql = MessageFormat.format(SQL_LAYERS, value, source);
                    try (Statement statement = connection.createStatement()) {
                        ResultSet result = statement.executeQuery(sql);
                        if (result.next()) {
                            gzip.write(result.getBytes(1));
                        }
                    }
                }
            }
            gzip.close();
            return data.toByteArray();
        } catch (Exception e) {
            throw new TileException(e);
        }
    }

}
