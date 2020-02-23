package io.gazetteer.tiles.http;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.google.common.collect.Lists;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.gazetteer.tiles.Tile;
import io.gazetteer.tiles.TileException;
import io.gazetteer.tiles.TileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TileHandler implements HttpHandler {

  private static final Logger logger = LoggerFactory.getLogger(TileHandler.class);

  public static final List<String> TILE_ENCODING = Lists.newArrayList("gzip");

  public static final List<String> TILE_MIME_TYPE = Lists.newArrayList("application/vnd.mapbox-vector-tile");

  public static final List<String> ORIGIN_WILDCARD = Lists.newArrayList("*");

  private static final Pattern URL = Pattern.compile("/(\\d+)/(\\d+)/(\\d+)\\.pbf");

  private final TileReader tileReader;

  public TileHandler(TileReader tileReader) {
    this.tileReader = tileReader;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String path = exchange.getRequestURI().getPath();
    logger.info("GET {}", path);

    Matcher matcher = URL.matcher(path);
    if (!matcher.find()) {
      exchange.sendResponseHeaders(404, 0);
    }

    Integer z = Integer.parseInt(matcher.group(1));
    Integer x = Integer.parseInt(matcher.group(2));
    Integer y = Integer.parseInt(matcher.group(3));
    Tile tile = new Tile(x, y, z);

    try {
      byte[] bytes = tileReader.read(tile);
      exchange.getResponseHeaders().put(CONTENT_TYPE, TILE_MIME_TYPE);
      exchange.getResponseHeaders().put(CONTENT_ENCODING, TILE_ENCODING);
      exchange.getResponseHeaders().put(ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN_WILDCARD);
      exchange.getResponseHeaders().put(ACCESS_CONTROL_ALLOW_ORIGIN, Arrays.asList("*"));
      exchange.sendResponseHeaders(200, bytes.length);
      exchange.getResponseBody().write(bytes);
    } catch (TileException e) {
      e.printStackTrace();
      exchange.sendResponseHeaders(404, 0);
    } finally {
      exchange.close();
    }
  }
}
