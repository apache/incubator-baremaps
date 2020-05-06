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

import com.baremaps.cli.blueprint.ConfigFormatter;
import com.baremaps.tiles.config.Config;
import com.google.common.base.Charsets;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

public class ConfigHandler implements HttpHandler {

  private static Logger logger = LogManager.getLogger();

  private final Config config;

  public ConfigHandler(Config config) {
    this.config = config;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      ConfigFormatter configBuilder = new ConfigFormatter(this.config);
      Map<String, Object> config = configBuilder.format();

      Constructor constructor = new Constructor(Config.class);
      Representer representer = new Representer();
      DumperOptions options = new DumperOptions();
      options.setDefaultFlowStyle(FlowStyle.BLOCK);
      options.setSplitLines(false);

      Yaml yaml = new Yaml(constructor, representer, options);
      byte[] bytes = yaml.dump(config).getBytes(Charsets.UTF_8);

      exchange.getResponseHeaders().put(CONTENT_TYPE, Arrays.asList("text/plain"));
      exchange.getResponseHeaders().put(CONTENT_ENCODING, Arrays.asList("utf-8"));
      exchange.sendResponseHeaders(200, bytes.length);
      exchange.getResponseBody().write(bytes);
    } catch (IOException ex) {
      logger.error("A problem occured {}", ex);
      exchange.sendResponseHeaders(404, 0);
    } finally {
      exchange.close();
    }
  }

}
