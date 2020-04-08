/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.tiles.http;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.google.common.collect.Lists;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.baremaps.core.tile.Tile;
import com.baremaps.tiles.TileException;
import com.baremaps.tiles.TileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TileHandler implements HttpHandler {

  private static Logger logger = LogManager.getLogger();

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

    int z = Integer.parseInt(matcher.group(1));
    int x = Integer.parseInt(matcher.group(2));
    int y = Integer.parseInt(matcher.group(3));
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
