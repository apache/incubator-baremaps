package com.baremaps.editor;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import com.baremaps.config.BlobMapper;
import com.baremaps.config.style.Style;
import com.baremaps.config.tileset.Tileset;
import com.baremaps.tile.Tile;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.TileStoreException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@javax.ws.rs.Path("/")
public class EditorService {

  private static Logger logger = LoggerFactory.getLogger(EditorService.class);

  @Inject
  @Named("tileset")
  private URI tileset;

  @Inject
  @Named("style")
  private URI style;

  @Inject
  private BlobMapper configStore;

  @Inject
  private Supplier<TileStore> tileStoreSupplier;

  private Sse sse;
  private OutboundSseEvent.Builder sseEventBuilder;
  private SseBroadcaster sseBroadcaster;

  private Thread fileWatcher;

  @Context
  public void setSse(Sse sse) {
    this.sse = sse;
    this.sseEventBuilder = sse.newEventBuilder();
    this.sseBroadcaster = sse.newBroadcaster();
    if (fileWatcher != null) {
      fileWatcher.interrupt();
    }
    fileWatcher = new Thread(() -> {
      try {
        Path tilesetFile = Paths.get(tileset.getPath()).toAbsolutePath();
        Path styleFile = Paths.get(style.getPath()).toAbsolutePath();
        WatchService watchService = FileSystems.getDefault().newWatchService();
        tilesetFile.getParent().register(watchService, ENTRY_MODIFY);
        styleFile.getParent().register(watchService, ENTRY_MODIFY);
        WatchKey key;
        while ((key = watchService.take()) != null) {
          Path dir = (Path) key.watchable();
          for (WatchEvent<?> event : key.pollEvents()) {
            Path path = dir.resolve((Path) event.context());
            ObjectNode jsonNode = configStore.read(style, ObjectNode.class);
            jsonNode.put("reload", path.endsWith(tilesetFile.getFileName()));
            sseBroadcaster.broadcast(sseEventBuilder.data(jsonNode.toString()).build());
          }
          key.reset();
        }
      } catch (InterruptedException e) {
        logger.error(e.getMessage());
      } catch (IOException e) {
        logger.error(e.getMessage());
      }
    });
    fileWatcher.start();
  }

  @GET
  @javax.ws.rs.Path("/changes")
  @Produces("text/event-stream")
  public void changes(@Context SseEventSink sseEventSink) {
    sseBroadcaster.register(sseEventSink);
  }

  @GET
  @javax.ws.rs.Path("style.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Style getStyle() throws IOException {
    Tileset tileset = configStore.read(this.tileset, Tileset.class);
    Style style = configStore.read(this.style, Style.class);

    // override style properties with tileset properties
    if (tileset.getCenter() != null) {
      style.setCenter(List.of(tileset.getCenter().getLon(), tileset.getCenter().getLat()));
      style.setZoom(tileset.getCenter().getZoom());
    }

    return style;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @javax.ws.rs.Path("style.json")
  public void putStyle(Style json) throws IOException {
    configStore.write(style, json);
  }

  @GET
  @javax.ws.rs.Path("tiles.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Tileset getTiles() throws IOException {
    Tileset tileset = configStore.read(this.tileset, Tileset.class);
    return tileset;
  }

  @PUT
  @javax.ws.rs.Path("tiles.json")
  public void putTiles(JsonNode json) throws IOException {
    configStore.write(style, json);
  }

  @GET
  @javax.ws.rs.Path("tiles/{z}/{x}/{y}.mvt")
  public Response tile(@PathParam("z") int z, @PathParam("x") int x, @PathParam("y") int y) {
    TileStore tileStore = tileStoreSupplier.get();
    Tile tile = new Tile(x, y, z);
    try {
      byte[] bytes = tileStore.read(tile);
      if (bytes != null) {
        return Response.status(200)
            .header(CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
            .header(CONTENT_ENCODING, "gzip")
            .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
            .entity(bytes)
            .build();
      } else {
        return Response.status(204).build();
      }
    } catch (TileStoreException ex) {
      logger.error(ex.getMessage());
      return Response.status(404).build();
    }
  }

}
