/*
 * Copyright (C) 2020 The Baremaps Authors
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

package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.config.Config;
import com.baremaps.config.ConfigLoader;
import com.baremaps.config.Query;
import com.baremaps.osm.postgres.PostgresHelper;
import com.baremaps.osm.progress.StreamProgress;
import com.baremaps.stream.StreamUtils;
import com.baremaps.tile.Tile;
import com.baremaps.tile.TileBatcher;
import com.baremaps.tile.TileBlobStore;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.TileStoreException;
import com.baremaps.tile.Tiler;
import com.baremaps.tile.mbtiles.MBTiles;
import com.baremaps.tile.postgres.PostgisTileStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "export", description = "Export vector tiles from the database.")
public class Export implements Callable<Integer> {

  private static Logger logger = LoggerFactory.getLogger(Export.class);

  @Mixin
  private Options options;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the Postgres database.",
      required = true)
  private String database;

  @Option(
      names = {"--config"},
      paramLabel = "YAML",
      description = "The YAML source configuration file.",
      required = true)
  private URI source;

  @Option(
      names = {"--repository"},
      paramLabel = "URL",
      description = "The tile repository URL.",
      required = true)
  private URI repository;

  @Option(
      names = {"--tiles"},
      paramLabel = "TILES",
      description = "The tiles to export.")
  private URI tiles;

  @Option(
      names = {"--batch-array-size"},
      paramLabel = "BATCH_ARRAY_SIZE",
      description = "The size of the batch array.")
  private int batchArraySize = 1;

  @Option(
      names = {"--batch-array-index"},
      paramLabel = "READER",
      description = "The index of the batch in the array.")
  private int batchArrayIndex = 0;

  @Option(
      names = {"--mbtiles"},
      paramLabel = "MBTILES",
      description = "The repository is in the MBTiles format.")
  private boolean mbtiles = false;

  @Override
  public Integer call() throws TileStoreException, IOException {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());

    // Initialize the datasource
    DataSource datasource = PostgresHelper.datasource(database);

    // Initialize the blob store
    BlobStore blobStore = options.blobStore();

    // Read the configuration file
    logger.info("Reading configuration");
    Config source = new ConfigLoader(blobStore).load(this.source);

    logger.info("Initializing the source tile store");
    final TileStore tileSource = sourceTileStore(source, datasource);

    logger.info("Initializing the target tile store");
    final TileStore tileTarget = targetTileStore(source, blobStore);

    // Export the tiles
    logger.info("Generating the tiles");

    Stream<Tile> stream;
    if (tiles == null) {
      Envelope envelope = new Envelope(
          source.getBounds().getMinLon(), source.getBounds().getMaxLon(),
          source.getBounds().getMinLat(), source.getBounds().getMaxLat());
      long count = Tile.count(envelope,
          (int) source.getBounds().getMinZoom(),
          (int) source.getBounds().getMaxZoom());
      stream = StreamUtils.stream(Tile.iterator(envelope,
          (int) source.getBounds().getMinZoom(),
          (int) source.getBounds().getMaxZoom()))
          .peek(new StreamProgress<>(count, 5000));
    } else {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(blobStore.read(tiles)))) {
        stream = reader.lines().flatMap(line -> {
          String[] array = line.split(",");
          int x = Integer.parseInt(array[0]);
          int y = Integer.parseInt(array[1]);
          int z = Integer.parseInt(array[2]);
          Tile tile = new Tile(x, y, z);
          return StreamUtils.stream(Tile.iterator(tile.envelope(),
              (int) source.getBounds().getMinZoom(),
              (int) source.getBounds().getMaxZoom()));
        });
      }
    }

    StreamUtils.batch(stream, 10)
        .filter(new TileBatcher(batchArraySize, batchArrayIndex))
        .forEach(new Tiler(tileSource, tileTarget));

    return 0;
  }

  private TileStore sourceTileStore(Config config, DataSource datasource) {
    return new PostgisTileStore(datasource, () -> config);
  }

  private TileStore targetTileStore(Config source, BlobStore blobStore)
      throws TileStoreException, IOException {
    if (mbtiles) {
      SQLiteDataSource dataSource = new SQLiteDataSource();
      dataSource.setUrl("jdbc:sqlite:" + repository.getPath());
      MBTiles tilesStore = new MBTiles(dataSource);
      tilesStore.initializeDatabase();
      tilesStore.writeMetadata(metadata(source));
      return tilesStore;
    } else {
      return new TileBlobStore(blobStore, repository);
    }
  }

  private Map<String, String> metadata(Config config) throws JsonProcessingException {
    Map<String, String> metadata = new HashMap<>();
    metadata.put("name", config.getId());
    metadata.put("version", config.getVersion().toString());
    metadata.put("description", config.getDescription());
    metadata.put("attribution", config.getAttribution());
    metadata.put("type", "baselayer");
    metadata.put("format", "pbf");
    metadata.put("center", String.format("%f, %f", config.getCenter().getLon(), config.getCenter().getLat()));
    metadata.put("bounds", String.format("%f, %f, %f, %f",
        config.getBounds().getMinLon(), config.getBounds().getMinLat(),
        config.getBounds().getMaxLon(), config.getBounds().getMaxLat()));
    metadata.put("minzoom", Double.toString(config.getBounds().getMinZoom()));
    metadata.put("maxzoom", Double.toString(config.getBounds().getMaxZoom()));
    List<Map<String, Object>> layers = config.getLayers().stream().map(layer -> {
      Map<String, Object> map = new HashMap<>();
      map.put("id", layer.getId());
      map.put("description", layer.getDescription());
      map.put("minzoom", layer.getQueries().stream().mapToInt(Query::getMinZoom).min().getAsInt());
      map.put("maxzoom", layer.getQueries().stream().mapToInt(Query::getMaxZoom).max().getAsInt());
      return map;
    }).collect(Collectors.toList());
    metadata.put("json", new ObjectMapper().writeValueAsString(layers));
    return metadata;
  }

}
