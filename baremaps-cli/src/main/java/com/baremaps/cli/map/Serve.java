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

package com.baremaps.cli.map;

import static com.baremaps.server.ogcapi.Conversions.asPostgresQuery;
import static com.baremaps.server.utils.DefaultObjectMapper.defaultObjectMapper;
import static io.servicetalk.data.jackson.jersey.ServiceTalkJacksonSerializerFeature.contextResolverFor;

import com.baremaps.cli.Options;
import com.baremaps.database.tile.PostgresQuery;
import com.baremaps.database.tile.PostgresTileStore;
import com.baremaps.database.tile.TileCache;
import com.baremaps.database.tile.TileStore;
import com.baremaps.model.TileJSON;
import com.baremaps.postgres.PostgresUtils;
import com.baremaps.server.resources.ServerResources;
import com.baremaps.server.utils.CorsFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.servicetalk.http.api.BlockingStreamingHttpService;
import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import io.servicetalk.transport.api.ServerContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "serve", description = "Start a tile server with caching capabilities.")
public class Serve implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Serve.class);

  @Mixin private Options options;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of Postgres.",
      required = true)
  private String database;

  @Option(
      names = {"--cache"},
      paramLabel = "CACHE",
      description = "The caffeine cache directive.")
  private String cache = "";

  @Option(
      names = {"--tileset"},
      paramLabel = "TILESET",
      description = "The tileset file.",
      required = true)
  private Path tileset;

  @Option(
      names = {"--style"},
      paramLabel = "STYLE",
      description = "The style file.",
      required = true)
  private Path style;

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
    ObjectMapper objectMapper = defaultObjectMapper();
    TileJSON tileJSON = objectMapper.readValue(Files.readAllBytes(tileset), TileJSON.class);
    CaffeineSpec caffeineSpec = CaffeineSpec.parse(cache);
    DataSource datasource = PostgresUtils.dataSource(database);

    List<PostgresQuery> queries = asPostgresQuery(tileJSON);
    TileStore tileStore = new PostgresTileStore(datasource, queries);
    TileStore tileCache = new TileCache(tileStore, caffeineSpec);

    // Configure the application
    ResourceConfig application =
        new ResourceConfig()
            .register(CorsFilter.class)
            .register(ServerResources.class)
            .register(contextResolverFor(objectMapper))
            .register(
                new AbstractBinder() {
                  @Override
                  protected void configure() {
                    bind(tileset).to(Path.class).named("tileset");
                    bind(style).to(Path.class).named("style");
                    bind(tileCache).to(TileStore.class);
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
