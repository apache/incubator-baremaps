
package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.FileBlobStore;
import com.baremaps.config.Config;
import com.baremaps.config.YamlStore;
import com.baremaps.osm.postgres.PostgresHelper;
import com.baremaps.server.JsonService;
import com.baremaps.server.TemplateService;
import com.baremaps.server.TileService;
import com.baremaps.server.transfer.Tileset;
import com.baremaps.tile.TileCache;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgisTileStore;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.file.FileService;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
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
  private URI style;

  @Option(
      names = {"--assets"},
      paramLabel = "ASSETS",
      description = "A directory of static assets.",
      required = false)
  private Path assets;

  @Override
  public Integer call() throws IOException {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());

    logger.info("Initializing server");
    BlobStore blobStore = new FileBlobStore();
    Config config = new YamlStore(blobStore).read(this.config, Config.class);

    // TODO: Load mapbox style
    Object style = new Object();

    int threads = Runtime.getRuntime().availableProcessors();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(threads);
    ServerBuilder builder = Server.builder()
        .defaultHostname(config.getServer().getHost())
        .http(config.getServer().getPort())
        .blockingTaskExecutor(executor, true);

    logger.info("Initializing services");

    if (assets != null && Files.exists(assets)) {
      HttpService fileService = FileService.builder(assets).build();
      builder.service("/", fileService);
    } else {
      HttpService indexService = new TemplateService("index.ftl", () -> config);
      builder.service("/", indexService);

      HttpService styleService = new JsonService(() -> style);
      builder.service("/style.json", styleService);

      HttpService faviconService = FileService.of(ClassLoader.getSystemClassLoader(), "/favicon.ico");
      builder.service("/favicon.ico", faviconService);
    }

    CaffeineSpec caffeineSpec = CaffeineSpec.parse(config.getServer().getCache());
    DataSource datasource = PostgresHelper.datasource(database);
    TileStore tileStore = new PostgisTileStore(datasource, () -> config);
    TileStore tileCache = new TileCache(tileStore, caffeineSpec);

    Object tileset = new Tileset().toTileset(config);
    Supplier<Object> tilesetSupplier = () -> tileset;
    builder.annotatedService("/tiles/", new TileService(tileCache));

    logger.info("Start server");
    Server server = builder.build();
    server.start();

    return 0;
  }

}