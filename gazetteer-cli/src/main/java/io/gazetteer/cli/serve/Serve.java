package io.gazetteer.cli.serve;

import com.sun.net.httpserver.HttpServer;
import io.gazetteer.common.postgis.DatabaseUtil;
import io.gazetteer.tilestore.model.TileReader;
import io.gazetteer.tilestore.postgis.PostgisConfig;
import io.gazetteer.tilestore.postgis.PostgisLayer;
import io.gazetteer.tilestore.postgis.PostgisTileReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.dbcp2.PoolingDataSource;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name="serve")
public class Serve implements Callable<Integer> {

  @Parameters(index = "0", paramLabel = "CONFIG_FILE", description = "The YAML configuration config.")
  private Path config;

  @Parameters(index = "1", paramLabel = "POSTGRES_DATABASE", description = "The Postgres database.")
  private String database;

  @Override
  public Integer call() throws IOException {
    // Read the configuration toInputStream
    List<PostgisLayer> layers = PostgisConfig.load(new FileInputStream(config.toFile())).getLayers();
    PoolingDataSource datasource = DatabaseUtil.poolingDataSource(database);
    TileReader tileReader = new PostgisTileReader(datasource, layers);

    // Create the http server
    HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
    server.createContext("/tiles/", new TileHandler(tileReader));
    server.createContext("/", new ResourceHandler());
    server.setExecutor(null);
    server.start();

    return 0;
  }
}
