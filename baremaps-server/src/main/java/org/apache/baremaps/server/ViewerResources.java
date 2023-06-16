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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import org.apache.baremaps.config.ConfigReader;
import org.apache.baremaps.vectortile.style.Style;
import org.apache.baremaps.vectortile.tilejson.TileJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@javax.ws.rs.Path("/")
public class ViewerResources {

  private static final Logger logger = LoggerFactory.getLogger(ViewerResources.class);

  private final ConfigReader configReader = new ConfigReader();

  private final Path style;

  private final Path tileset;

  private final ObjectMapper objectMapper;

  private final SseBroadcaster sseBroadcaster;

  private final OutboundSseEvent.Builder sseEventBuilder;

  @Inject
  public ViewerResources(
      @Named("tileset") Path tileset,
      @Named("style") Path style,
      ObjectMapper objectMapper,
      Sse sse) {
    this.tileset = tileset.toAbsolutePath();
    this.style = style.toAbsolutePath();
    this.objectMapper = objectMapper;
    this.sseBroadcaster = sse.newBroadcaster();
    this.sseEventBuilder = sse.newEventBuilder();

    // Observe the file system for changes
    var directories = new HashSet<>(Arrays.asList(tileset.getParent(), style.getParent()));
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
  public TileJSON getTileset() throws IOException {
    var config = configReader.read(tileset);
    var object = objectMapper.readValue(config, TileJSON.class);
    return object;
  }
}
