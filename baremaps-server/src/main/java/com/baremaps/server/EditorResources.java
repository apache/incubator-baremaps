package com.baremaps.server;

import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import com.baremaps.blob.BlobStore;
import com.baremaps.config.BlobMapper;
import com.baremaps.config.BlobMapperException;
import com.baremaps.config.style.Style;
import com.baremaps.config.tileset.Tileset;
import com.baremaps.tile.Tile;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgresQuery;
import com.baremaps.tile.postgres.PostgresTileStore;
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
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
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
public class EditorResources {

  private static final Logger logger = LoggerFactory.getLogger(EditorResources.class);

  private final URI style;

  private final URI tileset;

  private final BlobStore blobStore;

  private final DataSource dataSource;

  private SseBroadcaster sseBroadcaster;
  private Thread fileWatcher;

  @Inject
  public EditorResources(@Named("style") URI style, @Named("tileset") URI tileset, BlobStore blobStore,
      DataSource dataSource) {
    this.tileset = tileset;
    this.style = style;
    this.blobStore = blobStore;
    this.dataSource = dataSource;
  }

  @Context
  public void setSse(Sse sse) {
    OutboundSseEvent.Builder sseEventBuilder = sse.newEventBuilder();
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
            ObjectNode jsonNode = new BlobMapper(blobStore).read(style, ObjectNode.class);
            jsonNode.put("reload", path.endsWith(tilesetFile.getFileName()));
            sseBroadcaster.broadcast(sseEventBuilder.data(jsonNode.toString()).build());
          }
          key.reset();
        }
      } catch (InterruptedException e) {
        logger.error(e.getMessage());
        Thread.currentThread().interrupt();
      } catch (BlobMapperException | IOException e) {
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

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @javax.ws.rs.Path("style.json")
  public void putStyle(Style json) throws BlobMapperException {
    new BlobMapper(blobStore).write(style, json);
  }

  @PUT
  @javax.ws.rs.Path("tiles.json")
  public void putTiles(JsonNode json) throws BlobMapperException {
    new BlobMapper(blobStore).write(style, json);
  }

  @GET
  @javax.ws.rs.Path("style.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Style getStyle() throws BlobMapperException {
    return new BlobMapper(blobStore).read(style, Style.class);
  }

  @GET
  @javax.ws.rs.Path("tiles.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Tileset getTileset() throws BlobMapperException {
    return new BlobMapper(blobStore).read(tileset, Tileset.class);
  }

  @GET
  @javax.ws.rs.Path("/tiles/{z}/{x}/{y}.mvt")
  public Response getTile(@PathParam("z") int z, @PathParam("x") int x, @PathParam("y") int y) {
    try {
      List<PostgresQuery> queries = Mappers.map(getTileset());
      TileStore tileStore = new PostgresTileStore(dataSource, queries);
      Tile tile = new Tile(x, y, z);
      byte[] bytes = tileStore.read(tile);
      if (bytes != null) {
        return Response.status(200)
            .header(CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
            .header(CONTENT_ENCODING, "gzip")
            .entity(bytes)
            .build();
      } else {
        return Response.status(204).build();
      }
    } catch (Exception ex) {
      logger.error("Tile error", ex);
      return Response.status(404).build();
    }
  }

}
