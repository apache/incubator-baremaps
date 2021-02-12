
package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.FileBlobStore;
import com.baremaps.config.Config;
import com.baremaps.config.ConfigLoader;
import com.baremaps.osm.postgres.PostgresHelper;
import com.baremaps.server.BlueprintMapper;
import com.baremaps.server.ChangePublisher;
import com.baremaps.server.JsonService;
import com.baremaps.server.StyleMapper;
import com.baremaps.server.TemplateService;
import com.baremaps.server.TileService;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgisTileStore;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.file.FileService;
import com.linecorp.armeria.server.streaming.ServerSentEvents;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
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

@Command(name = "preview", description = "Preview the vector tiles.")
public class Preview implements Callable<Integer> {

  private static Logger logger = LoggerFactory.getLogger(Preview.class);

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
      paramLabel = "CONFIG",
      description = "The configuration file.",
      required = true)
  private Path config;

  private Server server;

  @Override
  public Integer call() throws IOException {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());

    BlobStore blobStore = new FileBlobStore();
    Supplier<Config> configSupplier = () -> {
      try {
        URI uri = new URI(config.toAbsolutePath().toString());
        return new ConfigLoader(blobStore).load(uri);
      } catch (URISyntaxException e) {
        logger.error("Unable to create an URI from the configuration file.", e);
      } catch (IOException e) {
        logger.error("Unable to read the configuration file.", e);
      } catch (Exception e) {
        logger.error("An error occured with the configuration file. ", e);
      }
      return null;
    };

    DataSource datasource = PostgresHelper.datasource(database);
    TileStore tileStore = new PostgisTileStore(datasource, configSupplier);

    logger.info("Initializing server");
    Config config = configSupplier.get();
    String host = config.getServer().getHost();
    int port = config.getServer().getPort();
    int threads = Runtime.getRuntime().availableProcessors();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(threads);

    logger.info("Initializing services");
    HttpService faviconService = FileService.of(ClassLoader.getSystemClassLoader(), "/favicon.ico");
    HttpService indexService = new TemplateService("index.ftl", configSupplier);
    HttpService tileService = new TileService(tileStore);
    HttpService styleService = new JsonService(config.getStylesheets().isEmpty()
        ? () -> new BlueprintMapper().apply(configSupplier.get())
        : () -> new StyleMapper().apply(configSupplier.get()));

    logger.info("Build routes");
    ServerBuilder builder = Server.builder()
        .defaultHostname(host)
        .http(port)
        .service("/", indexService)
        .service("/favicon.ico", faviconService)
        .service("/style.json", styleService)
        .service("regex:^/tiles/(?<z>[0-9]+)/(?<x>[0-9]+)/(?<y>[0-9]+).pbf$", tileService)
        .blockingTaskExecutor(executor, true);

    // Keep a connection open with the browser.
    // When the server restarts, for instance when a change occurs in the configuration,
    // The browser reloads the webpage and displays the changes.
    logger.info("Listen for changes");
    ChangePublisher publisher = new ChangePublisher(this.config.toAbsolutePath().getParent());
    builder.service("/changes/", (ctx, req) -> {
      ctx.clearRequestTimeout();
      return ServerSentEvents.fromPublisher(publisher);
    });

    logger.info("Start server");
    server = builder.build();
    server.start();

    return 0;
  }

}