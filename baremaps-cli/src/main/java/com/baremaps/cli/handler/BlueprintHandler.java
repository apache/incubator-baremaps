/*
 * Copyright (C) 2020 The Baremaps Authors
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
package com.baremaps.cli.handler;

import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.baremaps.cli.blueprint.TemplateFormatter;
import com.baremaps.tiles.config.Config;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlueprintHandler implements HttpHandler {

  private static Logger logger = LogManager.getLogger();

  public final Config config;

  public BlueprintHandler(Config config) {
    this.config = config;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      URL url = Resources.getResource("blueprint.html");
      String template = Resources.toString(url, Charsets.UTF_8);

      ImmutableMap<String, Object> params = ImmutableMap.<String, Object>builder()
          .put("lon", config.getCenter().getLon())
          .put("lat", config.getCenter().getLat())
          .put("zoom", config.getCenter().getZoom())
          .put("bearing", config.getCenter().getBearing())
          .put("pitch", config.getCenter().getPitch())
          .put("minZoom", config.getBounds().getMinZoom())
          .put("maxZoom", config.getBounds().getMaxZoom())
          .build();
      String result = TemplateFormatter.format(template, params);
      byte[] bytes = result.getBytes(Charsets.UTF_8);

      exchange.getResponseHeaders().put(CONTENT_TYPE, Arrays.asList("text/html"));
      exchange.getResponseHeaders().put(CONTENT_ENCODING, Arrays.asList("utf-8"));
      exchange.sendResponseHeaders(200, bytes.length);
      exchange.getResponseBody().write(bytes);
    } catch (IOException ex) {
      logger.error(ex);
      exchange.sendResponseHeaders(404, 0);
    } finally {
      exchange.close();
    }
  }

}
