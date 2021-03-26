
package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.FileBlobStore;
import com.baremaps.config.Config;
import com.baremaps.osm.postgres.PostgresHelper;
import com.baremaps.server.StyleService;
import com.baremaps.server.TemplateService;
import com.baremaps.server.TileService;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgisTileStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.cors.CorsService;
import com.linecorp.armeria.server.file.FileService;
import java.io.IOException;
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
      required = false)
  private Path config;

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

    Config config = new ObjectMapper(new YAMLFactory()).readValue(this.config.toFile(), Config.class);

    int threads = Runtime.getRuntime().availableProcessors();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(threads);
    ServerBuilder builder = Server.builder()
        .defaultHostname(config.getServer().getHost())
        .http(config.getServer().getPort())
        .blockingTaskExecutor(executor, true);

    logger.info("Initializing services");

    HttpService previewService = new TemplateService("preview.ftl", config);
    builder.service("/", previewService);

    HttpService compareService = new TemplateService("compare.ftl", config);
    builder.service("/compare/", compareService);

    HttpService faviconService = FileService.of(ClassLoader.getSystemClassLoader(), "/favicon.ico");
    builder.service("/favicon.ico", faviconService);

    builder.annotatedService(new StyleService(style));

    DataSource datasource = PostgresHelper.datasource(database);
    TileStore tileStore = new PostgisTileStore(datasource, config);

    builder.annotatedService(new TileService(tileStore));

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