package io.gazetteer.cli.commands;

import com.sun.net.httpserver.HttpServer;
import io.gazetteer.osm.database.PostgisHelper;
import io.gazetteer.tiles.TileReader;
import io.gazetteer.tiles.http.ResourceHandler;
import io.gazetteer.tiles.http.TileHandler;
import io.gazetteer.tiles.postgis.PostgisConfig;
import io.gazetteer.tiles.postgis.PostgisTileReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import org.apache.commons.dbcp2.PoolingDataSource;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "serve")
public class Serve implements Callable<Integer> {

  @Parameters(
      index = "0",
      paramLabel = "CONFIG_FILE",
      description = "The YAML configuration config.")
  private File file;

  @Parameters(
      index = "1",
      paramLabel = "POSTGRES_DATABASE",
      description = "The Postgres database.")
  private String database;

  @Option(
      names = {"-p", "--port"},
      description = "The port on which to listen.")
  private int port = 9000;

  @Override
  public Integer call() throws IOException {
    // Read the configuration toInputStream
    PostgisConfig config = PostgisConfig.load(new FileInputStream(file));
    PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);
    TileReader tileReader = new PostgisTileReader(datasource, config);

    // Create the http server
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/tiles/", new TileHandler(tileReader));
    server.createContext("/", new ResourceHandler());
    server.setExecutor(null);
    server.start();

    return 0;
  }
}
