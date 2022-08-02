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

package com.baremaps.cli.ogcapi;

import static com.baremaps.server.utils.DefaultObjectMapper.defaultObjectMapper;
import static io.servicetalk.data.jackson.jersey.ServiceTalkJacksonSerializerFeature.contextResolverFor;

import com.baremaps.cli.Options;
import com.baremaps.postgres.PostgresUtils;
import com.baremaps.server.ogcapi.ApiResource;
import com.baremaps.server.ogcapi.CollectionsResource;
import com.baremaps.server.ogcapi.ConformanceResource;
import com.baremaps.server.ogcapi.RootResource;
import com.baremaps.server.ogcapi.StylesResource;
import com.baremaps.server.ogcapi.SwaggerResource;
import com.baremaps.server.ogcapi.TilesetsResource;
import com.baremaps.server.resources.ImportResource;
import com.baremaps.server.resources.StudioResource;
import com.baremaps.server.utils.CorsFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.servicetalk.http.api.BlockingStreamingHttpService;
import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import io.servicetalk.transport.api.ServerContext;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Config;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "ogcapi", description = "OGC API server (experimental).")
public class OgcApi implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(OgcApi.class);

  @Mixin private Options options;

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
    ObjectMapper mapper = defaultObjectMapper();
    mapper.readValue("", JsonNode.class);

    // Configure jdbi and set the ObjectMapper
    DataSource datasource = PostgresUtils.dataSource(this.database);
    Jdbi jdbi =
        Jdbi.create(datasource)
            .installPlugin(new PostgresPlugin())
            .installPlugin(new Jackson2Plugin())
            .configure(Jackson2Config.class, config -> config.setMapper(mapper));

    // Initialize the application
    ResourceConfig application =
        new ResourceConfig()
            .registerClasses(
                SwaggerResource.class,
                RootResource.class,
                CorsFilter.class,
                ConformanceResource.class,
                CollectionsResource.class,
                StylesResource.class,
                TilesetsResource.class,
                StudioResource.class,
                ImportResource.class,
                MultiPartFeature.class)
            .register(new ApiResource("studio-openapi.yaml"))
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
