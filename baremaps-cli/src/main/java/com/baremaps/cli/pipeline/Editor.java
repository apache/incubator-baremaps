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

package com.baremaps.cli.pipeline;

import static com.baremaps.server.utils.DefaultObjectMapper.defaultObjectMapper;
import static io.servicetalk.data.jackson.jersey.ServiceTalkJacksonSerializerFeature.contextResolverFor;

import com.baremaps.blob.ConfigBlobStore;
import com.baremaps.cli.Options;
import com.baremaps.core.postgres.PostgresUtils;
import com.baremaps.server.resources.DevelopmentResources;
import com.baremaps.server.utils.CorsFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.servicetalk.http.api.BlockingStreamingHttpService;
import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import io.servicetalk.transport.api.ServerContext;
import java.net.URI;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
    name = "editor",
    description = "Start a development server for editing a map with maputnik.")
public class Editor implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Editor.class);

  @Mixin private Options options;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the Postgres database.",
      required = true)
  private String database;

  @Option(
      names = {"--tileset"},
      paramLabel = "TILESET",
      description = "The tileset file.",
      required = true)
  private URI tileset;

  @Option(
      names = {"--style"},
      paramLabel = "STYLE",
      description = "The style file.",
      required = true)
  private URI style;

  @Option(
      names = {"--host"},
      paramLabel = "HOST",
      description = "The host of the server.")
  private String host = "localhost";

  @Option(
      names = {"--port"},
      paramLabel = "PORT",
      description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {
    ConfigBlobStore blobStore = new ConfigBlobStore(options.blobStore());
    DataSource dataSource = PostgresUtils.datasource(database);

    // Configure serialization
    ObjectMapper objectMapper = defaultObjectMapper();

    // Configure the application
    ResourceConfig application =
        new ResourceConfig()
            .register(CorsFilter.class)
            .register(DevelopmentResources.class)
            .register(contextResolverFor(objectMapper))
            .register(
                new AbstractBinder() {
                  @Override
                  protected void configure() {
                    bind("editor").to(String.class).named("assets");
                    bind(tileset).to(URI.class).named("tileset");
                    bind(style).to(URI.class).named("style");
                    bind(blobStore).to(ConfigBlobStore.class);
                    bind(dataSource).to(DataSource.class);
                    bind(objectMapper).to(ObjectMapper.class);
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
