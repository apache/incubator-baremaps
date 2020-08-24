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
package com.baremaps.cli.service;

import com.baremaps.tiles.config.Config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.AbstractHttpService;
import com.linecorp.armeria.server.ServiceRequestContext;
import java.util.Map;
import javax.inject.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigService extends AbstractHttpService {

  private static Logger logger = LogManager.getLogger();

  private final Provider<Config> config;

  public ConfigService(Provider<Config> config) {
    this.config = config;
  }

  @Override
  protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws JsonProcessingException {
    ConfigBuilder configFormatter = new ConfigBuilder(this.config.get());
    Map<String, Object> config = configFormatter.format();
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
        .disable(Feature.WRITE_DOC_START_MARKER)
        .disable(Feature.SPLIT_LINES));
    return HttpResponse.of(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config));
  }

}
