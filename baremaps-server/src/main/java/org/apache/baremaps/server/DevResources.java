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

package org.apache.baremaps.server;

import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import org.apache.baremaps.database.tile.PostgresTileStore;
import org.apache.baremaps.database.tile.Tile;
import org.apache.baremaps.database.tile.TileStore;
import org.apache.baremaps.style.Style;
import org.apache.baremaps.tileset.Tileset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@javax.ws.rs.Path("/")
public class DevResources {

  private static final Logger logger = LoggerFactory.getLogger(DevResources.class);

  private final ConfigReader configReader = new ConfigReader();

  private final Path style;

  private final Path tileset;

  private final DataSource dataSource;

  private final ObjectMapper objectMapper;

  private final Sse sse;

  private final SseBroadcaster sseBroadcaster;

  private final OutboundSseEvent.Builder sseEventBuilder;

  public static final String TILE_ENCODING = "gzip";

  public static final String TILE_TYPE = "application/vnd.mapbox-vector-tile";

  @Inject
  public DevResources(@Named("tileset") Path tileset, @Named("style") Path style,
      DataSource dataSource, ObjectMapper objectMapper, Sse sse) {
    this.tileset = tileset.toAbsolutePath();
    this.style = style.toAbsolutePath();
    this.dataSource = dataSource;
    this.objectMapper = objectMapper;
    this.sse = sse;
    this.sseBroadcaster = sse.newBroadcaster();
    this.sseEventBuilder = sse.newEventBuilder();

    // Observe the file system for changes
    Set<Path> directories = new HashSet<>(Arrays.asList(tileset.getParent(), style.getParent()));
    new Thread(new DirectoryWatcher(directories, this::broadcastChanges)).start();
  }

  public void broadcastChanges(Path path) {
    try {
      var value = configReader.read(style);
      var styleObjectNode = objectMapper.readValue(value, ObjectNode.class);

      // reload the page if changes affected the tileset
      styleObjectNode.put("reload", path.endsWith(tileset.getFileName()));

      // broadcast the changes
      sseBroadcaster.broadcast(sseEventBuilder.data(styleObjectNode.toString()).build());
    } catch (IOException e) {
      logger.error("Unable to broadcast change", e);
    }
  }

  @GET
  @javax.ws.rs.Path("changes")
  @Produces("text/event-stream")
  public void changes(@Context SseEventSink sseEventSink) {
    sseBroadcaster.register(sseEventSink);
  }

  @GET
  @javax.ws.rs.Path("style.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Style getStyle() throws IOException {
    var config = configReader.read(style);
    var object = objectMapper.readValue(config, Style.class);
    return object;
  }

  @GET
  @javax.ws.rs.Path("tiles.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Tileset getTileset() throws IOException {
    var config = configReader.read(tileset);
    var object = objectMapper.readValue(config, Tileset.class);
    return object;
  }

  @GET
  @javax.ws.rs.Path("/tiles/{z}/{x}/{y}.mvt")
  public Response getTile(@PathParam("z") int z, @PathParam("x") int x, @PathParam("y") int y) {
    try {
      TileStore tileStore = new PostgresTileStore(dataSource, getTileset());
      Tile tile = new Tile(x, y, z);
      ByteBuffer blob = tileStore.read(tile);
      if (blob != null) {
        return Response.status(200).header(CONTENT_TYPE, TILE_TYPE)
            .header(CONTENT_ENCODING, TILE_ENCODING).entity(blob.array()).build();
      } else {
        return Response.status(204).build();
      }
    } catch (Exception ex) {
      logger.error("Tile error", ex);
      return Response.status(404).build();
    }
  }

  @GET
  @javax.ws.rs.Path("{path:.*}")
  public Response get(@PathParam("path") String path) throws IOException {
    if (path.equals("") || path.endsWith("/")) {
      path += "viewer.html";
    }
    path = String.format("assets/%s", path);
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
      var bytes = inputStream.readAllBytes();
      return Response.ok().entity(bytes).build();
    } catch (IOException e) {
      return Response.status(404).build();
    }
  }
}
