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
import com.baremaps.util.tile.Tile;
import com.baremaps.tiles.TileReader;
import com.baremaps.tiles.TileWriter;
import com.baremaps.tiles.config.Config;
import com.baremaps.tiles.file.FileTileStore;
import com.baremaps.tiles.postgis.FastTileReader;
import com.baremaps.tiles.postgis.SlowTileReader;
import com.baremaps.tiles.s3.S3TileStore;
import com.baremaps.tiles.util.TileUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.io.ParseException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import software.amazon.awssdk.services.s3.S3Client;

@Command(name = "export", description = "Export vector tiles from the Postgresql database.")
public class Export implements Callable<Integer> {

  private static Logger logger = LogManager.getLogger();

  @Mixin
  private Mixins mixins;

  @Option(
      names = {"--database"},
      paramLabel = "JDBC",
      description = "The JDBC url of the Postgres database.",
      required = true)
  private String database;

  @Option(
      names = {"--config"},
      paramLabel = "YAML",
      description = "The YAML configuration file.",
      required = true)
  private URI config;

  @Option(
      names = {"--repository"},
      paramLabel = "URL",
      description = "The tile repository URL.",
      required = true)
  private String repository;

  @Option(
      names = {"--minZoom"},
      paramLabel = "MIN",
      description = "The minimal zoom level.")
  private int minZoom = 0;

  @Option(
      names = {"--maxZoom"},
      paramLabel = "MAX",
      description = "The maximal zoom level.")
  private int maxZoom = 14;

  @Option(
      names = {"--delta"},
      paramLabel = "DELTA",
      description = "The input delta file.")
  private URI delta;

  @Option(
      names = {"--reader"},
      paramLabel = "READER",
      description = "The tile reader.")
  private TileReaderOption tileReader = slow;

  @Override
  public Integer call() throws SQLException, ParseException, IOException {
    Configurator.setRootLevel(Level.getLevel(mixins.level));
    logger.info("{} processors available.", Runtime.getRuntime().availableProcessors());

    // Initialize the file system
    FileSystem fileSystem = FileSystem.getDefault(mixins.caching);

    // Read the configuration file
    try (InputStream input = fileSystem.read(this.config)) {
      Config config = Config.load(input);
      PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);

      // Initialize tile reader and writer
      TileReader tileReader = tileReader(datasource, config);
      TileWriter tileWriter = tileWriter(repository);

      // Export the tiles
      Stream<Tile> tiles = tileStream(fileSystem, datasource);
      tiles.parallel().forEach(tile -> {
        try {
          byte[] bytes = tileReader.read(tile);
          tileWriter.write(tile, bytes);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }

    return 0;
  }

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

  private TileWriter tileWriter(String repository) throws IOException {
    if (Files.exists(Paths.get(repository))) {
      return new FileTileStore(Paths.get(repository));
    } else if (repository.startsWith("s3://")) {
      try {
        URI uri = new URI(repository);
        String bucket = uri.getHost();
        String root = uri.getPath().substring(1);
        S3Client client = S3Client.builder().build();
        return new S3TileStore(client, bucket, root);
      } catch (URISyntaxException e) {
        throw new IOException("Wrong repository url.");
      }
    } else {
      throw new IOException("Wrong repository url.");
    }
  }

  private Stream<Tile> tileStream(FileSystem fileSystem, DataSource datasource)
      throws IOException, SQLException, ParseException {
    if (delta != null) {
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(fileSystem.read(delta)))) {
        Stream<String> lines = reader.lines();
        return lines.flatMap(line -> {
          String[] array = line.split(",");
          int x = Integer.parseInt(array[0]);
          int y = Integer.parseInt(array[1]);
          int z = Integer.parseInt(array[2]);
          Tile tile = new Tile(x, y, z);
          return Tile.getTiles(tile.envelope(), minZoom, maxZoom);
        });
      }
    } else {
      try (Connection connection = datasource.getConnection()) {
        Envelope envelope = TileUtil.envelope(connection);
        return Tile.getTiles(envelope, minZoom, maxZoom);
      }
    }
  }

}
