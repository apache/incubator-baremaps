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

package com.baremaps.cli;

import static io.servicetalk.data.jackson.jersey.ServiceTalkJacksonSerializerFeature.contextResolverFor;

import com.baremaps.openapi.resources.CollectionsService;
import com.baremaps.openapi.resources.ConformanceService;
import com.baremaps.openapi.resources.RootService;
import com.baremaps.openapi.resources.StylesService;
import com.baremaps.openapi.resources.TilesetsService;
import com.baremaps.postgres.jdbc.PostgresUtils;
import com.baremaps.server.CorsFilter;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.servicetalk.http.api.BlockingStreamingHttpService;
import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import io.servicetalk.transport.api.ServerContext;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Config;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "openapi", description = "Serve an openapi endpoint (experimental).")
public class OpenApi implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(OpenApi.class);

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the Postgres database.",
      required = true)
  private String database;

  @Option(
      names = {"--port"},
      paramLabel = "PORT",
      description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {
    // Configure serialization
    ObjectMapper mapper =
        new ObjectMapper()
            .configure(Feature.IGNORE_UNKNOWN, true)
            .setSerializationInclusion(Include.NON_NULL)
            .setSerializationInclusion(Include.NON_EMPTY);

    // Configure jdbi and set the ObjectMapper
    DataSource datasource = PostgresUtils.datasource(this.database);
    Jdbi jdbi =
        Jdbi.create(datasource)
            .installPlugin(new PostgresPlugin())
            .installPlugin(new Jackson2Plugin())
            .configure(Jackson2Config.class, config -> config.setMapper(mapper));

    // Initialize the application
    ResourceConfig application =
        new ResourceConfig()
            .registerClasses(
                ApiListingResource.class,
                RedocResource.class,
                SwaggerResource.class,
                RootService.class,
                CorsFilter.class,
                ConformanceService.class,
                CollectionsService.class,
                StylesService.class,
                TilesetsService.class)
            .register(contextResolverFor(mapper))
            .register(
                new AbstractBinder() {
                  @Override
                  protected void configure() {
                    bind(datasource).to(DataSource.class);
                    bind(jdbi).to(Jdbi.class);
                  }
                });

    BlockingStreamingHttpService httpService =
        new HttpJerseyRouterBuilder().buildBlockingStreaming(application);
    ServerContext serverContext =
        HttpServers.forPort(port).listenBlockingStreamingAndAwait(httpService);

    logger.info("Listening on {}", serverContext.listenAddress());

    serverContext.awaitShutdown();

    return 0;
  }
}
