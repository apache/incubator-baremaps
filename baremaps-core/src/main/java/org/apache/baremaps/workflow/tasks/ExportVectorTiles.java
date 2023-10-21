/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.workflow.tasks;

import static org.apache.baremaps.utils.ObjectMapperUtils.objectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.baremaps.config.ConfigReader;
import org.apache.baremaps.stream.ProgressLogger;
import org.apache.baremaps.stream.StreamUtils;
import org.apache.baremaps.tilestore.*;
import org.apache.baremaps.tilestore.file.FileTileStore;
import org.apache.baremaps.tilestore.mbtiles.MBTilesStore;
import org.apache.baremaps.tilestore.postgres.PostgresTileStore;
import org.apache.baremaps.utils.SqliteUtils;
import org.apache.baremaps.vectortile.tileset.Tileset;
import org.apache.baremaps.vectortile.tileset.TilesetQuery;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ExportVectorTiles(
    Path tileset,
    Path repository,
    int batchArraySize,
    int batchArrayIndex,
    Format format) implements Task {

  public enum Format {
    file,
    mbtiles
  }

  private static final Logger logger = LoggerFactory.getLogger(ExportVectorTiles.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var configReader = new ConfigReader();
    var objectMapper = objectMapper();
    var tileset = objectMapper.readValue(configReader.read(this.tileset), Tileset.class);
    var datasource = context.getDataSource(tileset.getDatabase());

    var sourceTileStore = sourceTileStore(tileset, datasource);
    var targetTileStore = targetTileStore(tileset);

    var envelope = tileset.getBounds().size() == 4
        ? new Envelope(
            tileset.getBounds().get(0), tileset.getBounds().get(2),
            tileset.getBounds().get(1), tileset.getBounds().get(3))
        : new Envelope(-180, 180, -85.0511, 85.0511);

    var count = TileCoord.count(envelope, tileset.getMinzoom(), tileset.getMaxzoom());
    var start = System.currentTimeMillis();


    var tileCoordIterator =
        TileCoord.iterator(envelope, tileset.getMinzoom(), tileset.getMaxzoom());
    var tileCoordStream =
        StreamUtils.stream(tileCoordIterator).peek(new ProgressLogger<>(count, 5000));
    var bufferedTileEntryStream = StreamUtils.bufferInCompletionOrder(tileCoordStream, tile -> {
      try {
        return new TileEntry(tile, sourceTileStore.read(tile));
      } catch (TileStoreException e) {
        throw new RuntimeException(e);
      }
    }, 1000);
    var partitionedTileEntryStream = StreamUtils.partition(bufferedTileEntryStream, 1000);
    partitionedTileEntryStream.forEach(batch -> {
      try {
        targetTileStore.write(batch);
      } catch (TileStoreException e) {
        throw new RuntimeException(e);
      }
    });

    var stop = System.currentTimeMillis();
    logger.info("Exported {} tiles in {}s", count, (stop - start) / 1000);
  }

  private TileStore sourceTileStore(Tileset tileset, DataSource datasource) {
    return new PostgresTileStore(datasource, tileset);
  }

  private TileStore targetTileStore(Tileset source) throws TileStoreException, IOException {
    switch (format) {
      case file:
        return new FileTileStore(repository);
      case mbtiles:
        Files.deleteIfExists(repository);
        var dataSource = SqliteUtils.createDataSource(repository, false);
        var tilesStore = new MBTilesStore(dataSource);
        tilesStore.initializeDatabase();
        tilesStore.writeMetadata(metadata(source));
        return tilesStore;
      default:
        throw new IllegalArgumentException("Unsupported format");
    }
  }

  private Map<String, String> metadata(Tileset tileset) throws JsonProcessingException {
    var metadata = new HashMap<String, String>();

    metadata.put("name", tileset.getName());
    metadata.put("version", tileset.getVersion());
    metadata.put("description", tileset.getDescription());
    metadata.put("attribution", tileset.getAttribution());
    metadata.put("type", "baselayer");
    metadata.put("format", "pbf");
    metadata.put(
        "center",
        tileset.getCenter().stream().map(Number::toString).collect(Collectors.joining(", ")));
    metadata.put(
        "bounds",
        tileset.getBounds().stream().map(Object::toString).collect(Collectors.joining(", ")));
    metadata.put("minzoom", Double.toString(tileset.getMinzoom()));
    metadata.put("maxzoom", Double.toString(tileset.getMaxzoom()));

    var layers =
        tileset.getVectorLayers().stream()
            .map(
                layer -> {
                  Map<String, Object> map = new HashMap<>();
                  map.put("id", layer.getId());
                  map.put("description", layer.getDescription());
                  map.put(
                      "minzoom",
                      layer.getQueries().stream().mapToInt(TilesetQuery::getMinzoom).min()
                          .getAsInt());
                  map.put(
                      "maxzoom",
                      layer.getQueries().stream().mapToInt(TilesetQuery::getMaxzoom).max()
                          .getAsInt());
                  return map;
                })
            .toList();

    metadata.put("json", new ObjectMapper().writeValueAsString(layers));

    return metadata;
  }

}
