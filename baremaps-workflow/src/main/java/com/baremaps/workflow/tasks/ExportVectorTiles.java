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

package com.baremaps.workflow.tasks;

import static com.baremaps.server.ogcapi.Conversions.asPostgresQuery;

import com.baremaps.database.postgres.PostgresUtils;
import com.baremaps.database.tile.FileTileStore;
import com.baremaps.database.tile.MBTiles;
import com.baremaps.database.tile.PostgresQuery;
import com.baremaps.database.tile.PostgresTileStore;
import com.baremaps.database.tile.Tile;
import com.baremaps.database.tile.TileBatchPredicate;
import com.baremaps.database.tile.TileChannel;
import com.baremaps.database.tile.TileStore;
import com.baremaps.database.tile.TileStoreException;
import com.baremaps.model.Query;
import com.baremaps.model.TileJSON;
import com.baremaps.osm.progress.StreamProgress;
import com.baremaps.stream.StreamUtils;
import com.baremaps.workflow.Task;
import com.baremaps.workflow.WorkflowException;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Envelope;
import org.sqlite.SQLiteDataSource;

public record ExportVectorTiles(
    String id,
    List<String> needs,
    String database,
    String tileset,
    String repository,
    int batchArraySize,
    int batchArrayIndex,
    boolean mbtiles)
    implements Task {

  @Override
  public void run() {
    try {
      ObjectMapper mapper =
          new ObjectMapper()
              .configure(Feature.IGNORE_UNKNOWN, true)
              .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
              .setSerializationInclusion(Include.NON_NULL)
              .setSerializationInclusion(Include.NON_EMPTY);

      DataSource datasource = PostgresUtils.dataSource(database);

      TileJSON source = mapper.readValue(Files.readAllBytes(Paths.get(tileset)), TileJSON.class);
      TileStore tileSource = sourceTileStore(source, datasource);
      TileStore tileTarget = targetTileStore(source);

      Envelope envelope =
          new Envelope(
              source.getBounds().get(0), source.getBounds().get(2),
              source.getBounds().get(1), source.getBounds().get(3));
      long count = Tile.count(envelope, source.getMinzoom(), source.getMaxzoom());
      Stream<Tile> stream =
          StreamUtils.stream(Tile.iterator(envelope, source.getMinzoom(), source.getMaxzoom()))
              .peek(new StreamProgress<>(count, 5000));

      StreamUtils.batch(stream, 10)
          .filter(new TileBatchPredicate(batchArraySize, batchArrayIndex))
          .forEach(new TileChannel(tileSource, tileTarget));
    } catch (Exception exception) {
      throw new WorkflowException(exception);
    }
  }

  private TileStore sourceTileStore(TileJSON tileset, DataSource datasource) {
    List<PostgresQuery> queries = asPostgresQuery(tileset);
    return new PostgresTileStore(datasource, queries);
  }

  private TileStore targetTileStore(TileJSON source) throws TileStoreException, IOException {
    if (mbtiles) {
      SQLiteDataSource dataSource = new SQLiteDataSource();
      dataSource.setUrl("jdbc:sqlite:" + repository);
      MBTiles tilesStore = new MBTiles(dataSource);
      tilesStore.initializeDatabase();
      tilesStore.writeMetadata(metadata(source));
      return tilesStore;
    } else {
      return new FileTileStore(Paths.get(repository));
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
            .toList();
    metadata.put("json", new ObjectMapper().writeValueAsString(layers));
    return metadata;
  }
}
