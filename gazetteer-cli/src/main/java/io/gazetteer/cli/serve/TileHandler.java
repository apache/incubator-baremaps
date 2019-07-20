package io.gazetteer.cli.serve;

import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.google.common.collect.Lists;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.gazetteer.tilestore.model.Tile;
import io.gazetteer.tilestore.model.TileException;
import io.gazetteer.tilestore.model.TileReader;
import io.gazetteer.tilestore.model.XYZ;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TileHandler implements HttpHandler {

  public static final List<String> TILE_ENCODING = Lists.newArrayList("gzip");

  public static final List<String> TILE_MIME_TYPE = Lists.newArrayList("application/vnd.mapbox-vector-tile");

  private static final Pattern URL = Pattern.compile("/(\\d+)/(\\d+)/(\\d+)\\.pbf");

  private final TileReader tileReader;

  public TileHandler(TileReader tileReader) {
    this.tileReader = tileReader;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    Matcher matcher = URL.matcher(exchange.getRequestURI().getPath());
    if (!matcher.find()) {
      exchange.sendResponseHeaders(404, 0);
    }

    Integer z = Integer.parseInt(matcher.group(1));
    Integer x = Integer.parseInt(matcher.group(2));
    Integer y = Integer.parseInt(matcher.group(3));
    XYZ xyz = new XYZ(x, y, z);

    try {
      Tile tile = tileReader.read(xyz);
      exchange.getResponseHeaders().put(CONTENT_TYPE, TILE_MIME_TYPE);
      exchange.getResponseHeaders().put(CONTENT_ENCODING, TILE_ENCODING);
      exchange.sendResponseHeaders(200, tile.getBytes().length);
      exchange.getResponseBody().write(tile.getBytes());
    } catch (TileException e) {
      e.printStackTrace();
      exchange.sendResponseHeaders(404, 0);
    } finally {
      exchange.close();
    }
  }
}
