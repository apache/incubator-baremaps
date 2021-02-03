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
package com.baremaps.server;

import com.baremaps.postgres.config.Config;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.AbstractHttpService;
import com.linecorp.armeria.server.ServiceRequestContext;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StyleService extends AbstractHttpService {

  private static Logger logger = LoggerFactory.getLogger(StyleService.class);

  private final Supplier<Config> config;

  public StyleService(Supplier<Config> config) {
    this.config = config;
  }

  @Override
  protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws JsonProcessingException {
    StyleBuilder builder = new StyleBuilder(config.get());
    Map<String, Object> style = builder.build();
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    String output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(style);
    return HttpResponse.of(output);
  }

}
