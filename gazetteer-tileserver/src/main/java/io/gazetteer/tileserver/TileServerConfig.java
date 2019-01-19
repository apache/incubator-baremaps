package io.gazetteer.tileserver;

import io.gazetteer.core.TileSource;
import io.gazetteer.mbtiles.MBTilesDataSource;
import io.gazetteer.postgis.PostgisConfig;
import io.gazetteer.postgis.PostgisTileSource;
import io.gazetteer.postgis.PostgisLayer;
import io.netty.handler.ssl.SslContext;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TileServerConfig {

    public final String host;

    public final int port;

    public final SslContext sslContext;

    public final TileSource dataSource;

    public final Pattern tileUri;

    public TileServerConfig(String host, int port, SslContext sslContext, TileSource dataSource, Pattern tileUri) {
        this.host = host;
        this.port = port;
        this.sslContext = sslContext;
        this.dataSource = dataSource;
        this.tileUri = tileUri;
    }

    public static TileServerConfig fromMBTilesFile(File mbtiles) throws SQLException {
        String host = "localhost";
        int port = 8081;
        String url = String.format("jdbc:sqlite:%s", mbtiles.getPath());
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        dataSource.setReadOnly(true);
        dataSource.setSharedCache(true);
        dataSource.setPageSize(1024);
        dataSource.setCacheSize(10000);
        MBTilesDataSource cache = MBTilesDataSource.fromDataSource(dataSource);
        // todo: get the format from the metadata
        Pattern tileUri =  Pattern.compile(String.format("/(\\d{1,2})/(\\d{1,6})/(\\d{1,6}).%s", "pbf"));
        return new TileServerConfig(host, port, null, cache, tileUri);
    }

    public static TileServerConfig fromPGTiles(File file) throws FileNotFoundException {
        String host = "localhost";
        int port = 8081;
        List<PostgisLayer> layers = PostgisConfig.load(new FileInputStream(file)).getLayers();
        PostgisTileSource cache = new PostgisTileSource(layers);
        Pattern tileUri =  Pattern.compile(String.format("/(\\d{1,2})/(\\d{1,6})/(\\d{1,6}).pbf"));
        return new TileServerConfig(host, port, null, cache, tileUri);
    }

}
