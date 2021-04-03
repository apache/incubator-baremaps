
package com.baremaps.cli;

import com.baremaps.blob.FileBlobStore;
import com.baremaps.config.BlobMapper;
import com.baremaps.config.Config;
import com.baremaps.osm.postgres.PostgresHelper;
import com.baremaps.server.EditorService;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgisTileStore;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.cors.CorsService;
import com.linecorp.armeria.server.logging.LoggingService;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "preview", description = "Preview and edit the vector tiles.")
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
      description = "The tileset file.",
      required = true)
  private URI config;

  @Option(
      names = {"--style"},
      paramLabel = "STYLE",
      description = "The style file.",
      required = true)
  private URI style;

  @Override
  public Integer call() throws IOException {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());

    BlobMapper mapper = new BlobMapper(new FileBlobStore());
    Config config = mapper.read(this.config, Config.class);
    DataSource dataSource = PostgresHelper.datasource(database);
    Supplier<TileStore> tileStoreSupplier = () -> {
      try {
        return new PostgisTileStore(dataSource, mapper.read(this.config, Config.class));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };

    Server.builder()
        .defaultHostname(config.getServer().getHost())
        .http(config.getServer().getPort())
        .annotatedService(new EditorService(mapper, this.config, this.style, tileStoreSupplier))
        .decorator(CorsService.builderForAnyOrigin()
            .allowRequestMethods(HttpMethod.POST, HttpMethod.GET, HttpMethod.PUT)
            .allowRequestHeaders("Origin", "Content-Type", "Accept")
            .newDecorator())
        .decorator(LoggingService.newDecorator())
        .disableServerHeader()
        .disableDateHeader()
        .build()
        .start();

    return 0;
  }

}