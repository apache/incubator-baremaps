package com.baremaps.server;

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
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.common.sse.ServerSentEvent;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.Blocking;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.ProducesEventStream;
import com.linecorp.armeria.server.annotation.ProducesJson;
import com.linecorp.armeria.server.annotation.Put;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Sinks;

public class EditorService {

  private static final ResponseHeaders headers = ResponseHeaders.builder(200)
      .add(CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
      .add(CONTENT_ENCODING, "gzip")
      .add(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
      .build();

  private static Logger logger = LoggerFactory.getLogger(EditorService.class);

  private final Sinks.Many<ServerSentEvent> changes = Sinks.many().multicast().directBestEffort();

  private final BlobMapper configStore;

  private final URI config;

  private final URI style;

  private final Supplier<TileStore> tileStoreSupplier;

  public EditorService(BlobMapper configStore, URI config, URI style, Supplier<TileStore> tileStoreSupplier) {
    this.configStore = configStore;
    this.config = config;
    this.style = style;
    this.tileStoreSupplier = tileStoreSupplier;
    monitorChanges();
  }

  public void monitorChanges() {
    new Thread(() -> {
      try {
        Path configFile = Paths.get(config.getPath()).toAbsolutePath();
        Path styleFile = Paths.get(style.getPath()).toAbsolutePath();
        WatchService watchService = FileSystems.getDefault().newWatchService();
        configFile.getParent().register(watchService, ENTRY_MODIFY);
        styleFile.getParent().register(watchService, ENTRY_MODIFY);
        WatchKey key;
        while ((key = watchService.take()) != null) {
          Path dir = (Path) key.watchable();
          for (WatchEvent<?> event : key.pollEvents()) {
            Path path = dir.resolve((Path) event.context());
            ObjectNode jsonNode = configStore.read(style, ObjectNode.class);
            jsonNode.put("reload", path.endsWith(configFile.getFileName()));
            changes.tryEmitNext(ServerSentEvent.ofData(jsonNode.toString()));
          }
          key.reset();
        }
      } catch (InterruptedException e) {
        logger.error(e.getMessage());
      } catch (IOException e) {
        logger.error(e.getMessage());
      }
    }).start();
  }

  @Get("/changes")
  @ProducesEventStream
  public Publisher<ServerSentEvent> changes(ServiceRequestContext ctx) throws IOException {
    ctx.clearRequestTimeout();
    return changes.asFlux();
  }

  @Get("/style.json")
  @ProducesJson
  public Style getStyle() throws IOException {
    return configStore.read(style, Style.class);
  }

  @Put("/style.json")
  public void putStyle(Style json) throws IOException {
    configStore.write(style, json);
  }

  @Get("/tiles.json")
  @ProducesJson
  public Tileset getTiles() throws IOException {
    return configStore.read(config, Tileset.class);
  }

  @Put("/tiles.json")
  public void putTiles(JsonNode json) throws IOException {
    configStore.write(style, json);
  }

  @Get("regex:^/tiles/(?<z>[0-9]+)/(?<x>[0-9]+)/(?<y>[0-9]+).mvt$")
  @Blocking
  public HttpResponse tile(@Param("z") int z, @Param("x") int x, @Param("y") int y) {
    TileStore tileStore = tileStoreSupplier.get();
    Tile tile = new Tile(x, y, z);
    try {
      byte[] bytes = tileStore.read(tile);
      if (bytes != null) {
        HttpData data = HttpData.wrap(bytes);
        return HttpResponse.of(headers, data);
      } else {
        return HttpResponse.of(204);
      }
    } catch (TileStoreException ex) {
      logger.error(ex.getMessage());
      return HttpResponse.of(404);
    }
  }

}
