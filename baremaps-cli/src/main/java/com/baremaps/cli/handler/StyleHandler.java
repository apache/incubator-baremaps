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

import com.baremaps.cli.blueprint.BlueprintBuilder;
import com.baremaps.tiles.config.Config;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StyleHandler implements HttpHandler {

  private static Logger logger = LogManager.getLogger();

  private final Config config;

  public StyleHandler(Config config) {
    this.config = config;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      BlueprintBuilder builder = new BlueprintBuilder(config);
      Map<String, Object> style = builder.build();

      Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
      byte[] bytes = gson.toJson(style).getBytes(Charsets.UTF_8);
      exchange.getResponseHeaders().put(CONTENT_TYPE, Arrays.asList("application/json"));
      exchange.getResponseHeaders().put(CONTENT_ENCODING, Arrays.asList("utf-8"));
      exchange.sendResponseHeaders(200, bytes.length);
      exchange.getResponseBody().write(bytes);
    } catch (Exception ex) {
      logger.error("A problem occured {}", ex);
      exchange.sendResponseHeaders(404, 0);
    } finally {
      exchange.close();
    }
  }

}
