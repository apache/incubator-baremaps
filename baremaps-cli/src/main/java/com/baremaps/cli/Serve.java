
package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.config.ConfigLoader;
import com.baremaps.config.source.Source;
import com.baremaps.config.source.SourceLoader;
import com.baremaps.config.style.Style;
import com.baremaps.config.style.StyleLoader;
import com.baremaps.osm.postgres.PostgresHelper;
import com.baremaps.server.BlueprintMapper;
import com.baremaps.server.ChangePublisher;
import com.baremaps.server.JsonService;
import com.baremaps.server.SourceMapper;
import com.baremaps.server.StyleMapper;
import com.baremaps.server.TemplateService;
import com.baremaps.server.TileService;
import com.baremaps.server.YamlService;
import com.baremaps.stream.SupplierUtils;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgisTileStore;
import com.linecorp.armeria.server.HttpService;
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
      names = {"--source"},
      paramLabel = "SOURCE",
      description = "The source configuration file.",
      required = true)
  private URI source;

  @Option(
      names = {"--style"},
      paramLabel = "STYLE",
      description = "The style configuration file.",
      required = false)
  private URI style;

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

    // Initialize the source supplier
    SourceLoader sourceLoader = new SourceLoader(blobStore);
    Source source = sourceLoader.load(this.source);
    Supplier<Source> sourceSupplier = supplier(sourceLoader, this.source, source);

    logger.info("Initializing datasource");
    DataSource datasource = PostgresHelper.datasource(database);

    logger.info("Initializing tile reader");
    final TileStore tileStore = new PostgisTileStore(datasource, sourceSupplier);

    logger.info("Initializing server");
    String host = source.getServer().getHost();
    int port = source.getServer().getPort();
    int threads = Runtime.getRuntime().availableProcessors();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(threads);

    HttpService faviconService = FileService.of(ClassLoader.getSystemClassLoader(), "/favicon.ico");
    HttpService sourceService = new YamlService(SupplierUtils.convert(sourceSupplier, new SourceMapper()));
    HttpService blueprintService = new JsonService(SupplierUtils.convert(sourceSupplier, new BlueprintMapper()));
    HttpService tileService = new TileService(tileStore);

    HttpService styleService = blueprintService;
    if (style != null) {
      StyleLoader styleLoader = new StyleLoader(blobStore);
      Style style = styleLoader.load(this.style);
      Supplier<Style> styleSupplier = supplier(styleLoader, this.style, style);
      styleService = new JsonService(SupplierUtils.convert(styleSupplier, new StyleMapper()));
    }

    ServerBuilder builder = Server.builder()
        .defaultHostname(host)
        .http(port)
        .service("/", new TemplateService(sourceSupplier))
        .service("/favicon.ico", faviconService)
        .service("/source.yaml", sourceService)
        .service("/style.json", styleService)
        .service("/blueprint.json", blueprintService)
        .service("regex:^/tiles/(?<z>[0-9]+)/(?<x>[0-9]+)/(?<y>[0-9]+).pbf$", tileService)
        .blockingTaskExecutor(executor, true);

    // Initialize the assets handler
    if (assets != null) {
      builder.service("/assets/", FileService.of(Paths.get(assets.getPath())));
    }

    // Keep a connection open with the browser.
    // When the server restarts, for instance when a change occurs in the configuration,
    // The browser reloads the webpage and displays the changes.
    Path directory = Paths.get(this.source.getPath()).toAbsolutePath().getParent();
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

  private <T> Supplier<T> supplier(ConfigLoader<T> loader, URI uri, T value) {
    if (watchChanges) {
      return () -> {
        try {
          return loader.load(uri);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      };
    } else {
      return () -> value;
    }
  }

}