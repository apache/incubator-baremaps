
package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.FileBlobStore;
import com.baremaps.config.Config;
import com.baremaps.config.yaml.YamlConfigReader;
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
  private URI config;

  @Override
  public Integer call() throws IOException {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());

    logger.info("Initializing server");
    BlobStore blobStore = new FileBlobStore();
    Supplier<Config> configSupplier = () -> {
      try {
        return new YamlConfigReader(blobStore).load(config);
      } catch (IOException e) {
        logger.error("Unable to read the configuration file.", e);
      } catch (Exception e) {
        logger.error("An error occured with the configuration file. ", e);
      }
      return null;
    };

    Config config = configSupplier.get();
    int threads = Runtime.getRuntime().availableProcessors();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(threads);
    ServerBuilder builder = Server.builder()
        .defaultHostname(config.getServer().getHost())
        .http(config.getServer().getPort())
        .blockingTaskExecutor(executor, true);

    logger.info("Initializing services");

    HttpService previewService = new TemplateService("preview.ftl", configSupplier);
    builder.service("/", previewService);

    HttpService compareService = new TemplateService("compare.ftl", configSupplier);
    builder.service("/compare/", compareService);

    HttpService faviconService = FileService.of(ClassLoader.getSystemClassLoader(), "/favicon.ico");
    builder.service("/favicon.ico", faviconService);

    HttpService styleService = new JsonService(config.getStylesheets().isEmpty()
        ? () -> new BlueprintMapper().apply(configSupplier.get())
        : () -> new StyleMapper().apply(configSupplier.get()));
    builder.service("/style.json", styleService);

    DataSource datasource = PostgresHelper.datasource(database);
    TileStore tileStore = new PostgisTileStore(datasource, configSupplier);
    HttpService tileService = new TileService(tileStore);
    builder.service("regex:^/tiles/(?<z>[0-9]+)/(?<x>[0-9]+)/(?<y>[0-9]+).pbf$", tileService);

    // Keep a connection open with the browser.
    // When the server restarts, for instance when a change occurs in the configuration,
    // The browser reloads the webpage and displays the changes.
    logger.info("Watch the configuration file for changes");
    Path watch = Paths.get(this.config.getPath()).toAbsolutePath().getParent();
    if (Files.exists(watch)) {
      ChangePublisher publisher = new ChangePublisher(watch);
      builder.service("/changes/", (ctx, req) -> {
        ctx.clearRequestTimeout();
        return ServerSentEvents.fromPublisher(publisher);
      });
    }

    logger.info("Start server");
    Server server = builder.build();
    server.start();

    return 0;
  }

}