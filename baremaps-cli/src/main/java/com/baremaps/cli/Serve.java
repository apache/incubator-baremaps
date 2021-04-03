
package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.FileBlobStore;
import com.baremaps.config.tileset.Tileset;
import com.baremaps.config.BlobMapper;
import com.baremaps.osm.postgres.PostgresHelper;
import com.baremaps.server.TileService;
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
    Tileset tileset = new BlobMapper(blobStore).read(this.config, Tileset.class);

    int threads = Runtime.getRuntime().availableProcessors();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(threads);
    ServerBuilder builder = Server.builder()
        .defaultHostname("localhost")
        .http(9000)
        .blockingTaskExecutor(executor, true);

    logger.info("Initializing services");

    if (assets != null && Files.exists(assets)) {
      HttpService fileService = FileService.builder(assets).build();
      builder.service("/", fileService);
    } else {
      HttpService faviconService = FileService.of(ClassLoader.getSystemClassLoader(), "/favicon.ico");
      builder.service("/favicon.ico", faviconService);
    }

    CaffeineSpec caffeineSpec = CaffeineSpec.parse("");
    DataSource datasource = PostgresHelper.datasource(database);
    TileStore tileStore = new PostgisTileStore(datasource, tileset);
    TileStore tileCache = new TileCache(tileStore, caffeineSpec);

    builder.annotatedService("/tiles/", new TileService(tileCache));

    logger.info("Start server");
    Server server = builder.build();
    server.start();

    return 0;
  }

}