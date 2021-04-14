package com.baremaps.editor;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.baremaps.config.style.Style;
import com.baremaps.config.tileset.Tileset;
import com.baremaps.tile.Tile;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.TileStoreException;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.Blocking;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.ProducesJson;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerService {

  private static Logger logger = LoggerFactory.getLogger(ServerService.class);

  private static final ResponseHeaders headers = ResponseHeaders.builder(200)
      .add(CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
      .add(CONTENT_ENCODING, "gzip")
      .add(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
      .build();

  private final String host;

  private final int port;

  private final Tileset tileset;

  private final Style style;

  private final TileStore tileStore;

  public ServerService(String host, int port, Tileset tileset, Style style, TileStore tileStore) {
    this.host = host;
    this.port = port;
    this.tileset = tileset;
    this.style = style;
    this.tileStore = tileStore;
  }

  @Get("/style.json")
  @ProducesJson
  public Style getStyle(ServiceRequestContext ctx) throws IOException {
    // override style properties with tileset properties
    if (tileset.getCenter() != null) {
      style.setCenter(List.of(tileset.getCenter().getLon(), tileset.getCenter().getLat()));
      style.setZoom(tileset.getCenter().getZoom());
    }

    // override style properties with server properties
    InetSocketAddress address = ctx.localAddress();
    style.setSources(Map.of("baremaps", Map.of(
        "type", "vector",
        "url", String.format("http://%s:%s/tiles.json", host, port))));

    return style;
  }

  @Get("/tiles.json")
  @ProducesJson
  public Tileset getTileset(ServiceRequestContext ctx) throws IOException {
    // override style properties with server properties
    tileset.setTiles(Arrays.asList(String.format("http://%s:%s/tiles/{z}/{x}/{y}.mvt", host, port)));

    return tileset;
  }

  @Get("regex:^/tiles/(?<z>[0-9]+)/(?<x>[0-9]+)/(?<y>[0-9]+).mvt$")
  @Blocking
  public HttpResponse tile(@Param("z") int z, @Param("x") int x, @Param("y") int y) {
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
