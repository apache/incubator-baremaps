/*
 * Copyright (C) 2011 The Baremaps Authors
 *
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

package com.baremaps.cli.commands;

import static com.baremaps.cli.options.TileReaderOption.slow;

import com.baremaps.cli.options.TileReaderOption;
import com.baremaps.util.fs.FileSystem;
import com.baremaps.util.postgis.PostgisHelper;
import com.baremaps.tiles.TileReader;
import com.baremaps.tiles.config.Config;
import com.baremaps.tiles.http.ResourceHandler;
import com.baremaps.tiles.http.TileHandler;
import com.baremaps.tiles.postgis.FastTileReader;
import com.baremaps.tiles.postgis.SlowTileReader;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "serve", description = "Serve vector tiles from the the Postgresql database.")
public class Serve implements Callable<Integer> {

  private static Logger logger = LogManager.getLogger();

  @Mixin
  private Mixins mixins;

  @Option(
      names = {"--database"},
      paramLabel= "JDBC",
      description = "The JDBC url of the Postgres database.",
      required = true)
  private String database;

  @Option(
      names = {"--config"},
      paramLabel= "YAML",
      description = "The YAML configuration file.",
      required = true)
  private URI config;

  @Option(
      names = {"--assets"},
      paramLabel = "ASSETS",
      description = "A directory containing assets.")
  private String directory;

  @Option(
      names = {"--port"},
      paramLabel = "PORT",
      description = "The port on which to listen.")
  private int port = 9000;

  @Option(
      names = {"--reader"},
      paramLabel = "READER",
      description = "The tile reader.")
  private TileReaderOption tileReader = slow;

  public TileReader tileReader(PoolingDataSource dataSource, Config config) {
    switch (tileReader) {
      case slow:
        return new SlowTileReader(dataSource, config);
      case fast:
        return new FastTileReader(dataSource, config);
      default:
        throw new UnsupportedOperationException("Unsupported tile reader");
    }
  }

  @Override
  public Integer call() throws IOException {
    Configurator.setRootLevel(Level.getLevel(mixins.level));

    logger.info("{} processors available.", Runtime.getRuntime().availableProcessors());

    // Read the configuration toInputStream
    logger.info("Reading configuration.");
    FileSystem fileReader = FileSystem.getDefault(mixins.caching);
    try(InputStream input = fileReader.read(this.config)) {
      Config config = Config.load(input);

      logger.info("Initializing datasource.");
      PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);

      // Choose the tile reader
      logger.info("Initializing tile reader.");
      TileReader tileReader = tileReader(datasource, config);

      // Create the http server
      logger.info("Initializing server.");
      HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
      server.createContext("/", new ResourceHandler(Paths.get(directory)));
      server.createContext("/tiles/", new TileHandler(tileReader));
      server.setExecutor(null);
      server.start();

      logger.info("Server started listening on port {}", port);
    }


    return 0;
  }


}
