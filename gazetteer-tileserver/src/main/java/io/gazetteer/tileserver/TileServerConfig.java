package io.gazetteer.tileserver;

import io.gazetteer.tileserver.mbtiles.MBTilesDataSource;
import io.gazetteer.tileserver.postgis.PGDataSource;
import io.gazetteer.tileserver.postgis.PGLayer;
import io.netty.handler.ssl.SslContext;
import org.sqlite.SQLiteDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TileServerConfig {

    public final String host;

    public final int port;

    public final SslContext sslContext;

    public final TileDataSource dataSource;

    public final Pattern tileUri;

    public TileServerConfig(String host, int port, SslContext sslContext, TileDataSource dataSource, Pattern tileUri) {
        this.host = host;
        this.port = port;
        this.sslContext = sslContext;
        this.dataSource = dataSource;
        this.tileUri = tileUri;
    }

    public static TileServerConfig fromMBTilesFile(String mbtiles) throws SQLException {
        String host = "localhost";
        int port = 8081;
        String url = String.format("jdbc:sqlite:%s", mbtiles);
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        dataSource.setReadOnly(true);
        dataSource.setSharedCache(true);
        dataSource.setPageSize(1024);
        dataSource.setCacheSize(10000);
        MBTilesDataSource cache = MBTilesDataSource.fromDataSource(dataSource);
        Pattern tileUri =  Pattern.compile(String.format("/(\\d{1,2})/(\\d{1,6})/(\\d{1,6}).%s", cache.metadata.format.name()));
        return new TileServerConfig(host, port, null, cache, tileUri);
    }

    public static TileServerConfig fromPGTiles() throws SQLException {
        String host = "localhost";
        int port = 8081;
        List<PGLayer> layers = new ArrayList<>();
        layers.add(new PGLayer("buildings", "polygon",
                "jdbc:postgresql://localhost:5432/osm?user=osm&password=osm",
                "SELECT ST_AsMVT(q, 'buildings', 4096, 'geom')\n" +
                "FROM (\n" +
                "  SELECT id,\n" +
                "    ST_AsMvtGeom(\n" +
                "      geom,\n" +
                "      ST_MakeEnvelope(?, ?, ?, ?),\n" +
                "      4096,\n" +
                "      256,\n" +
                "      true\n" +
                "    ) AS geom\n" +
                "  FROM ways\n" +
                "  WHERE geom && ST_MakeEnvelope(?, ?, ?, ?)\n" +
                "  AND ST_Intersects(geom, ST_MakeEnvelope(?, ?, ?, ?))\n" +
                ") AS q;", 0, 18));
        PGDataSource cache = new PGDataSource(layers);
        Pattern tileUri =  Pattern.compile(String.format("/(\\d{1,2})/(\\d{1,6})/(\\d{1,6}).pbf"));
        return new TileServerConfig(host, port, null, cache, tileUri);
    }

}
