package io.gazetteer.cli.commands;

import com.sun.net.httpserver.HttpServer;
import io.gazetteer.core.postgis.PostgisHelper;
import io.gazetteer.tiles.TileReader;
import io.gazetteer.tiles.config.Config;
import io.gazetteer.tiles.http.ResourceHandler;
import io.gazetteer.tiles.http.TileHandler;
import io.gazetteer.tiles.postgis.SimpleTileReader;
import io.gazetteer.tiles.postgis.WithTileReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.apache.commons.dbcp2.PoolingDataSource;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "serve")
public class Serve implements Callable<Integer> {

  @Parameters(
      index = "0",
      paramLabel = "POSTGRES_DATABASE",
      description = "The Postgres database.")
  private String database;

  @Parameters(
      index = "1",
      paramLabel = "CONFIG_FILE",
      description = "The YAML configuration config.")
  private Path file;

  @Parameters(
      index = "2",
      paramLabel = "STATIC_DIRECTORY",
      description = "The YAML configuration config.")
  private Path directory;

  @Option(
      names = {"-p", "--port"},
      description = "The port on which to listen.")
  private int port = 9000;

  @Option(
      names = {"-t", "--tile-reader"},
      description = "The tile reader.")
  private String tileReader = "basic";

  public TileReader initTileReader(PoolingDataSource dataSource, Config config) {
    switch (tileReader) {
      case "basic":
        return new SimpleTileReader(dataSource, config);
      case "with":
        return new WithTileReader(dataSource, config);
      default:
        throw new UnsupportedOperationException("Unsupported tile reader");
    }
  }

  @Override
  public Integer call() throws IOException {
    // Read the configuration toInputStream
    Config config = Config.load(new FileInputStream(file.toFile()));
    PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);

    // Choose the tile reader
    TileReader tileReader = initTileReader(datasource, config);

    // Create the http server
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/", new ResourceHandler(directory));
    server.createContext("/tiles/", new TileHandler(tileReader));
    server.setExecutor(null);
    server.start();

    return 0;
  }


}
