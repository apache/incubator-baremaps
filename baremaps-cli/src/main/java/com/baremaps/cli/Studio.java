
package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.FileBlobStore;
import com.baremaps.config.Config;
import com.baremaps.config.YamlStore;
import com.baremaps.osm.postgres.PostgresHelper;
import com.baremaps.server.BlueprintMapper;
import com.baremaps.server.JsonService;
import com.baremaps.server.StyleService;
import com.baremaps.server.TemplateService;
import com.baremaps.server.TileService;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgisTileStore;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.cors.CorsService;
import com.linecorp.armeria.server.file.FileService;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "studio", description = "Preview and edit the vector tiles.")
public class Studio implements Callable<Integer> {

  private static Logger logger = LoggerFactory.getLogger(Studio.class);

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

  @Option(
      names = {"--style"},
      paramLabel = "STYLE",
      description = "The style file.",
      required = false)
  private Path style;

  @Override
  public Integer call() throws IOException {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());

    logger.info("Initializing server");
    BlobStore blobStore = new FileBlobStore();
    Supplier<Config> configSupplier = () -> {
      try {
        return new YamlStore(blobStore).read(config, Config.class);
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

    HttpService blueprintService = new JsonService(() -> new BlueprintMapper().apply(configSupplier.get()));
    builder.service("/blueprint.json", blueprintService);

    builder.annotatedService(new StyleService(style));

    DataSource datasource = PostgresHelper.datasource(database);
    TileStore tileStore = new PostgisTileStore(datasource, configSupplier);
    HttpService tileService = new TileService(tileStore);
    builder.service("regex:^/tiles/(?<z>[0-9]+)/(?<x>[0-9]+)/(?<y>[0-9]+).pbf$", tileService);

    logger.info("Start server");
    Function<? super HttpService, CorsService> corsService =
        CorsService.builderForAnyOrigin()
            .allowRequestMethods(HttpMethod.POST, HttpMethod.GET, HttpMethod.PUT)
            .allowRequestHeaders("Origin", "Content-Type", "Accept")
            .newDecorator();
    Server server = builder.decorator(corsService).build();
    server.start();

    return 0;
  }

}