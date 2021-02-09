
package com.baremaps.cli;


import com.baremaps.blob.BlobStore;
import com.baremaps.config.legacy.Config;
import com.baremaps.config.legacy.ConfigLoader;
import com.baremaps.osm.postgres.PostgresHelper;
import com.baremaps.server.ChangePublisher;
import com.baremaps.server.ConfigService;
import com.baremaps.server.StyleService;
import com.baremaps.server.TemplateService;
import com.baremaps.server.TileService;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgisTileStore;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.file.FileService;
import com.linecorp.armeria.server.streaming.ServerSentEvents;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "serve", description = "Serve vector tiles from the database.")
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
      names = {"--config"},
      paramLabel = "YAML",
      description = "The YAML configuration file.",
      required = true)
  private URI config;

  @Option(
      names = {"--assets"},
      paramLabel = "ASSETS",
      description = "A directory containing assets.")
  private URI assets;

  @Option(
      names = {"--watch-changes"},
      paramLabel = "WATCH_CHANGES",
      description = "Watch for file changes.")
  private boolean watchChanges = false;

  private Server server;

  @Override
  public Integer call() throws IOException {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());

    BlobStore blobStore = options.blobStore();
    ConfigLoader configLoader = new ConfigLoader(blobStore);
    Supplier<Config> provider = () -> {
      try {
        return configLoader.load(this.config);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };

    Config config = provider.get();
    if (!watchChanges) {
      provider = () -> config;
    }

    logger.info("Initializing datasource");
    DataSource datasource = PostgresHelper.datasource(database);

    logger.info("Initializing tile reader");
    final TileStore tileStore = new PostgisTileStore(datasource, provider);

    logger.info("Initializing server");
    String host = config.getServer().getHost();
    int port = config.getServer().getPort();
    int threads = Runtime.getRuntime().availableProcessors();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(threads);
    ServerBuilder builder = Server.builder()
        .defaultHostname(host)
        .http(port)
        .service("/", new TemplateService(provider))
        .service("/favicon.ico",
            FileService.of(ClassLoader.getSystemClassLoader(), "/favicon.ico"))
        .service("/config.yaml", new ConfigService(provider))
        .service("/style.json", new StyleService(provider))
        .service("regex:^/tiles/(?<z>[0-9]+)/(?<x>[0-9]+)/(?<y>[0-9]+).pbf$",
            new TileService(tileStore))
        .blockingTaskExecutor(executor, true);

    // Initialize the assets handler if a path has been provided
    if (assets != null) {
      builder.service("/assets/", FileService.of(Paths.get(assets.getPath())));
    }

    // Keep a connection open with the browser.
    // When the server restarts, for instance when a change occurs in the configuration,
    // The browser reloads the webpage and displays the changes.
    Path directory = Paths.get(this.config.getPath()).toAbsolutePath().getParent();
    if (watchChanges && Files.exists(directory)) {
      ChangePublisher publisher = new ChangePublisher(directory);
      builder.service("/changes/", (ctx, req) -> {
        ctx.clearRequestTimeout();
        return ServerSentEvents.fromPublisher(publisher);
      });
    }

    server = builder.build();
    server.start();

    return 0;
  }

}