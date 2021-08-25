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

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.JsonBlobMapper;
import com.baremaps.model.MbStyle;
import com.baremaps.model.TileSet;
import com.baremaps.postgres.jdbc.PostgresUtils;
import com.baremaps.server.BlobResources;
import com.baremaps.server.CorsFilter;
import com.baremaps.server.Mappers;
import com.baremaps.server.ViewerResources;
import com.baremaps.tile.TileCache;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgresQuery;
import com.baremaps.tile.postgres.PostgresTileStore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.servicetalk.http.api.BlockingStreamingHttpService;
import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import io.servicetalk.transport.api.ServerContext;
import java.net.URI;
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

@Command(name = "serve", description = "Serve the vector tiles.")
public class Serve implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Serve.class);

  @Mixin private Options options;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the Postgres database.",
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
  private URI tileset;

  @Option(
      names = {"--style"},
      paramLabel = "STYLE",
      description = "The style file.",
      required = true)
  private URI style;

  @Option(
      names = {"--assets"},
      paramLabel = "ASSETS",
      description = "A directory of static assets.")
  private URI assets = URI.create("res://server/");

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
    BlobStore blobStore = options.blobStore();
    TileSet tilesetObject = new JsonBlobMapper(blobStore).read(this.tileset, TileSet.class);
    MbStyle styleObject = new JsonBlobMapper(blobStore).read(this.style, MbStyle.class);
    CaffeineSpec caffeineSpec = CaffeineSpec.parse(cache);
    DataSource datasource = PostgresUtils.datasource(database);

    List<PostgresQuery> queries = Mappers.map(tilesetObject);
    TileStore tileStore = new PostgresTileStore(datasource, queries);
    TileStore tileCache = new TileCache(tileStore, caffeineSpec);

    // Configure serialization
    ObjectMapper mapper =
        new ObjectMapper()
            .configure(Feature.IGNORE_UNKNOWN, true)
            .setSerializationInclusion(Include.NON_NULL)
            .setSerializationInclusion(Include.NON_EMPTY);

    // Configure the application
    ResourceConfig application =
        new ResourceConfig()
            .registerClasses(CorsFilter.class, ViewerResources.class, BlobResources.class)
            .register(contextResolverFor(mapper))
            .register(
                new AbstractBinder() {
                  @Override
                  protected void configure() {
                    bind(tilesetObject).to(TileSet.class);
                    bind(styleObject).to(MbStyle.class);
                    bind(tileCache).to(TileStore.class);
                    bind(blobStore).to(BlobStore.class);
                    bind(assets).named("assets").to(URI.class);
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
