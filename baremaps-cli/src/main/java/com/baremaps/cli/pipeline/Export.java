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

package com.baremaps.cli.pipeline;

import static com.baremaps.server.ogcapi.Conversions.asPostgresQuery;
import static com.baremaps.server.utils.DefaultObjectMapper.defaultObjectMapper;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.BlobStoreException;
import com.baremaps.cli.Options;
import com.baremaps.core.postgres.PostgresUtils;
import com.baremaps.core.tile.MBTiles;
import com.baremaps.core.tile.PostgresQuery;
import com.baremaps.core.tile.PostgresTileStore;
import com.baremaps.core.tile.Tile;
import com.baremaps.core.tile.TileBatchPredicate;
import com.baremaps.core.tile.TileBlobStore;
import com.baremaps.core.tile.TileChannel;
import com.baremaps.core.tile.TileStore;
import com.baremaps.core.tile.TileStoreException;
import com.baremaps.model.Query;
import com.baremaps.model.TileJSON;
import com.baremaps.osm.progress.StreamProgress;
import com.baremaps.stream.StreamUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "export", description = "Export vector tiles from the database.")
public class Export implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Export.class);

  @Mixin private Options options;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the Postgres database.",
      required = true)
  private String database;

  @Option(
      names = {"--tileset"},
      paramLabel = "TILESET",
      description = "The tileset file.",
      required = true)
  private URI tileset;

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
  public Integer call() throws TileStoreException, BlobStoreException, IOException {
    ObjectMapper mapper = defaultObjectMapper();
    DataSource datasource = PostgresUtils.datasource(database);
    BlobStore blobStore = options.blobStore();

    TileJSON source =
        mapper.readValue(blobStore.get(this.tileset).getInputStream(), TileJSON.class);
    TileStore tileSource = sourceTileStore(source, datasource);
    TileStore tileTarget = targetTileStore(source, blobStore);

    Stream<Tile> stream;
    if (tiles == null) {
      Envelope envelope =
          new Envelope(
              source.getBounds().get(0), source.getBounds().get(2),
              source.getBounds().get(1), source.getBounds().get(3));
      long count = Tile.count(envelope, source.getMinzoom(), source.getMaxzoom());
      stream =
          StreamUtils.stream(Tile.iterator(envelope, source.getMinzoom(), source.getMaxzoom()))
              .peek(new StreamProgress<>(count, 5000));
    } else {
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(blobStore.get(tiles).getInputStream()))) {
        stream =
            reader
                .lines()
                .flatMap(
                    line -> {
                      String[] array = line.split(",");
                      int x = Integer.parseInt(array[0]);
                      int y = Integer.parseInt(array[1]);
                      int z = Integer.parseInt(array[2]);
                      Tile tile = new Tile(x, y, z);
                      return StreamUtils.stream(
                          Tile.iterator(tile.envelope(), source.getMinzoom(), source.getMaxzoom()));
                    });
      }
    }

    logger.info("Exporting tiles");
    StreamUtils.batch(stream, 10)
        .filter(new TileBatchPredicate(batchArraySize, batchArrayIndex))
        .forEach(new TileChannel(tileSource, tileTarget));
    logger.info("Done");

    return 0;
  }

  private TileStore sourceTileStore(TileJSON tileset, DataSource datasource) {
    List<PostgresQuery> queries = asPostgresQuery(tileset);
    return new PostgresTileStore(datasource, queries);
  }

  private TileStore targetTileStore(TileJSON source, BlobStore blobStore)
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

  private Map<String, String> metadata(TileJSON tileset) throws JsonProcessingException {
    Map<String, String> metadata = new HashMap<>();
    metadata.put("name", tileset.getName());
    metadata.put("version", tileset.getVersion());
    metadata.put("description", tileset.getDescription());
    metadata.put("attribution", tileset.getAttribution());
    metadata.put("type", "baselayer");
    metadata.put("format", "pbf");
    metadata.put(
        "center",
        tileset.getCenter().stream().map(BigDecimal::toString).collect(Collectors.joining(", ")));
    metadata.put(
        "bounds",
        tileset.getBounds().stream().map(Object::toString).collect(Collectors.joining(", ")));
    metadata.put("minzoom", Double.toString(tileset.getMinzoom()));
    metadata.put("maxzoom", Double.toString(tileset.getMaxzoom()));
    List<Map<String, Object>> layers =
        tileset.getVectorLayers().stream()
            .map(
                layer -> {
                  Map<String, Object> map = new HashMap<>();
                  map.put("id", layer.getId());
                  map.put("description", layer.getDescription());
                  map.put(
                      "minzoom",
                      layer.getQueries().stream().mapToInt(Query::getMinzoom).min().getAsInt());
                  map.put(
                      "maxzoom",
                      layer.getQueries().stream().mapToInt(Query::getMaxzoom).max().getAsInt());
                  return map;
                })
            .collect(Collectors.toList());
    metadata.put("json", new ObjectMapper().writeValueAsString(layers));
    return metadata;
  }
}
