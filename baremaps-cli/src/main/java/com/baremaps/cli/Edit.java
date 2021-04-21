
package com.baremaps.cli;

import com.baremaps.config.BlobMapper;
import com.baremaps.config.tileset.Tileset;
import com.baremaps.osm.postgres.PostgresHelper;
import com.baremaps.editor.EditorService;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgisTileStore;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerListenerAdapter;
import com.linecorp.armeria.server.cors.CorsService;
import com.linecorp.armeria.server.file.FileService;
import java.awt.Desktop;
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

@Command(name = "edit", description = "Edit the vector tiles.")
public class Edit implements Callable<Integer> {

  private static Logger logger = LoggerFactory.getLogger(Edit.class);

  @Mixin
  private Options options;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the Postgres database.",
      required = true)
  private String database;

  @Option(
      names = {"--tileset"},
      paramLabel = "TILESET",
      description = "The tileset file.",
      required = true)
  private URI tileset;

  @Option(
      names = {"--style"},
      paramLabel = "STYLE",
      description = "The style file.",
      required = true)
  private URI style;

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

  @Option(
      names = {"--open"},
      paramLabel = "OPEN",
      description = "Open the browser.")
  private boolean open = false;

  @Override
  public Integer call() {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());

    BlobMapper mapper = new BlobMapper(options.blobStore());
    DataSource dataSource = PostgresHelper.datasource(database);
    Supplier<TileStore> tileStoreSupplier = () -> {
      try {
        return new PostgisTileStore(dataSource, mapper.read(this.tileset, Tileset.class));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };

    Server server = Server.builder()
        .defaultHostname(host)
        .http(port)
        .annotatedService(new EditorService(host, port, mapper, this.tileset, this.style, tileStoreSupplier))
        .serviceUnder("/", FileService.of(ClassLoader.getSystemClassLoader(), "/maputnik/"))
        .decorator(CorsService.builderForAnyOrigin()
            .allowRequestMethods(HttpMethod.POST, HttpMethod.GET, HttpMethod.PUT)
            .allowRequestHeaders("Origin", "Content-Type", "Accept")
            .newDecorator())
        .disableServerHeader()
        .disableDateHeader()
        .build();

    if (open) {
      server.addListener(new ServerListenerAdapter() {
        @Override
        public void serverStarted(Server server) throws Exception {
          if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(String.format("http://%s:%s/", host, port)));
          }
        }
      });
    }

    server.start();

    return 0;
  }

}