
package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.FileBlobStore;
import com.baremaps.config.style.Style;
import com.baremaps.config.tileset.Tileset;
import com.baremaps.config.BlobMapper;
import com.baremaps.osm.postgres.PostgresHelper;
import com.baremaps.server.ServerService;
import com.baremaps.tile.TileCache;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgisTileStore;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.cors.CorsService;
import com.linecorp.armeria.server.file.FileService;
import com.linecorp.armeria.server.logging.LoggingService;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
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
      names = {"--cache"},
      paramLabel = "CACHE",
      description = "The caffeine cache directive.")
  private String cache = "";

  @Option(
      names = {"--tileset"},
      paramLabel = "TILESET",
      description = "The tileset file.",
      required = true)
  private URI config;

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
  private Path assets;

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
  public Integer call() throws IOException {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));

    BlobStore blobStore = new FileBlobStore();
    Tileset tileset = new BlobMapper(blobStore).read(this.config, Tileset.class);
    Style style = new BlobMapper(blobStore).read(this.style, Style.class);

    CaffeineSpec caffeineSpec = CaffeineSpec.parse(cache);
    DataSource datasource = PostgresHelper.datasource(database);
    TileStore tileStore = new PostgisTileStore(datasource, tileset);
    TileStore tileCache = new TileCache(tileStore, caffeineSpec);

    ServerBuilder builder = Server.builder()
        .defaultHostname(host)
        .http(port)
        .decorator(CorsService.builderForAnyOrigin()
            .allowRequestMethods(HttpMethod.POST, HttpMethod.GET, HttpMethod.PUT)
            .allowRequestHeaders("Origin", "Content-Type", "Accept")
            .newDecorator())
        .decorator(LoggingService.newDecorator());

    if (assets != null && Files.exists(assets)) {
      HttpService fileService = FileService.builder(assets).build();
      builder.service("/", fileService);
    }

    builder.annotatedService(new ServerService(tileset, style, tileCache));

    Server server = builder.build();
    server.start();

    return 0;
  }

}