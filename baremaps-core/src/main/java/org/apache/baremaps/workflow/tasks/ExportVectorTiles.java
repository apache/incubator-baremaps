/*
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

package org.apache.baremaps.workflow.tasks;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.baremaps.database.tile.FileTileStore;
import org.apache.baremaps.database.tile.MBTiles;
import org.apache.baremaps.database.tile.PostgresTileStore;
import org.apache.baremaps.database.tile.Tile;
import org.apache.baremaps.database.tile.TileChannel;
import org.apache.baremaps.database.tile.TileStore;
import org.apache.baremaps.database.tile.TileStoreException;
import org.apache.baremaps.openstreetmap.utils.StreamProgress;
import org.apache.baremaps.stream.StreamUtils;
import org.apache.baremaps.tileset.Tileset;
import org.apache.baremaps.tileset.TilesetQuery;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

public record ExportVectorTiles(
  String database,
  String tileset,
  String repository,
  int batchArraySize,
  int batchArrayIndex,
  boolean mbtiles
) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ExportVectorTiles.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    logger.info("Exporting vector tiles from {} to {}", database, repository);
    var datasource = context.getDataSource(database);
    var mapper =
      new ObjectMapper()
        .configure(Feature.IGNORE_UNKNOWN, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(Include.NON_NULL)
        .setSerializationInclusion(Include.NON_EMPTY);

    var source = mapper.readValue(Files.readAllBytes(Paths.get(tileset)), Tileset.class);
    var tileSource = sourceTileStore(source, datasource);
    var tileTarget = targetTileStore(source);

    var envelope =
      new Envelope(
        source.getBounds().get(0), source.getBounds().get(2),
        source.getBounds().get(1), source.getBounds().get(3)
      );

    var count = Tile.count(envelope, source.getMinzoom(), source.getMaxzoom());

    var stream =
      StreamUtils.stream(Tile.iterator(envelope, source.getMinzoom(), source.getMaxzoom()))
        .peek(new StreamProgress<>(count, 5000));

    StreamUtils.batch(stream, 10).forEach(new TileChannel(tileSource, tileTarget));

    logger.info("Finished exporting vector tiles from {} to {}", database, repository);
  }

  private TileStore sourceTileStore(Tileset tileset, DataSource datasource) {
    return new PostgresTileStore(datasource, tileset);
  }

  private TileStore targetTileStore(Tileset source) throws TileStoreException, IOException {
    if (mbtiles) {
      var dataSource = new SQLiteDataSource();
      dataSource.setUrl("jdbc:sqlite:" + repository);

      var tilesStore = new MBTiles(dataSource);
      tilesStore.initializeDatabase();
      tilesStore.writeMetadata(metadata(source));

      return tilesStore;
    } else {
      return new FileTileStore(Paths.get(repository));
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
      tileset.getCenter().stream().map(Number::toString).collect(Collectors.joining(", "))
    );
    metadata.put(
      "bounds",
      tileset.getBounds().stream().map(Object::toString).collect(Collectors.joining(", "))
    );
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
              layer.getQueries().stream().mapToInt(TilesetQuery::getMinzoom).min().getAsInt()
            );
            map.put(
              "maxzoom",
              layer.getQueries().stream().mapToInt(TilesetQuery::getMaxzoom).max().getAsInt()
            );
            return map;
          })
        .toList();

    metadata.put("json", new ObjectMapper().writeValueAsString(layers));

    return metadata;
  }
}
