
package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.config.BlobMapper;
import com.baremaps.config.style.Style;
import com.baremaps.config.tileset.Tileset;
import com.baremaps.postgres.jdbc.PostgresUtils;
import com.baremaps.server.BlobResources;
import com.baremaps.server.ViewerResources;
import com.baremaps.tile.TileCache;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgresTileStore;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.servicetalk.http.api.BlockingStreamingHttpService;
import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import io.servicetalk.transport.api.ServerContext;
import java.net.URI;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "serve", description = "Serve the vector tiles.")
public class Serve implements Callable<Integer> {

  private static Logger logger = LoggerFactory.getLogger(Serve.class);

  @Mixin
  private Options options;

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
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));

    BlobStore blobStore = options.blobStore();

    Tileset tileset = new BlobMapper(blobStore).read(this.tileset, Tileset.class);
    Style style = new BlobMapper(blobStore).read(this.style, Style.class);

    CaffeineSpec caffeineSpec = CaffeineSpec.parse(cache);
    DataSource datasource = PostgresUtils.datasource(database);
    TileStore tileStore = new PostgresTileStore(datasource, tileset);
    TileStore tileCache = new TileCache(tileStore, caffeineSpec);

    ResourceConfig config = new ResourceConfig()
        .register(ViewerResources.class)
        .register(BlobResources.class)
        .register(new AbstractBinder() {
          @Override
          protected void configure() {
            bind(tileset).to(Tileset.class);
            bind(style).to(Style.class);
            bind(tileCache).to(TileStore.class);
            bind(blobStore).to(BlobStore.class);
            bind(assets).named("assets").to(URI.class);
          }
        });

    BlockingStreamingHttpService httpService = new HttpJerseyRouterBuilder()
        .buildBlockingStreaming(config);
    ServerContext serverContext = HttpServers.forPort(port)
        .listenBlockingStreamingAndAwait(httpService);

    logger.info("Listening on {}", serverContext.listenAddress());

    serverContext.awaitShutdown();

    return 0;
  }

}