package com.baremaps.cli.commands;

import com.baremaps.core.io.InputStreams;
import com.baremaps.tiles.postgis.SimpleTileReader;
import com.baremaps.tiles.postgis.WithTileReader;
import com.sun.net.httpserver.HttpServer;
import com.baremaps.core.postgis.PostgisHelper;
import com.baremaps.tiles.TileReader;
import com.baremaps.tiles.config.Config;
import com.baremaps.tiles.http.ResourceHandler;
import com.baremaps.tiles.http.TileHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.apache.commons.dbcp2.PoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "serve")
public class Serve implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Serve.class);

  @Parameters(
      index = "0",
      paramLabel = "POSTGRES_DATABASE",
      description = "The Postgres database.")
  private String database;

  @Parameters(
      index = "1",
      paramLabel = "CONFIG_FILE",
      description = "The YAML configuration config.")
  private String file;

  @Parameters(
      index = "2",
      paramLabel = "STATIC_DIRECTORY",
      description = "The YAML configuration config.")
  private String directory;

  @Option(
      names = {"--port"},
      description = "The port on which to listen.")
  private int port = 9000;

  @Option(
      names = {"--tile-reader"},
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
    logger.info("{} processors available.", Runtime.getRuntime().availableProcessors());

    // Read the configuration toInputStream
    Config config = Config.load(InputStreams.from(file));
    PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);

    // Choose the tile reader
    TileReader tileReader = initTileReader(datasource, config);

    // Create the http server
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/", new ResourceHandler(Paths.get(directory)));
    server.createContext("/tiles/", new TileHandler(tileReader));
    server.setExecutor(null);
    server.start();

    logger.info("Server started listening on port {}", port);

    return 0;
  }


}
