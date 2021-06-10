
package com.baremaps.cli;

import com.baremaps.config.BlobMapper;
import com.baremaps.config.tileset.Tileset;
import com.baremaps.editor.EditorApplication;
import com.baremaps.editor.EditorModule;
import com.baremaps.editor.ServerModule;
import com.baremaps.osm.postgres.PostgresHelper;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgisTileStore;
import io.servicetalk.http.api.BlockingStreamingHttpService;
import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import io.servicetalk.transport.api.ServerContext;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
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
  public Integer call() throws Exception {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.debug("{} processors available", Runtime.getRuntime().availableProcessors());

    BlobMapper mapper = new BlobMapper(options.blobStore());
    DataSource dataSource = PostgresHelper.datasource(database);
    Supplier<TileStore> tileStoreSupplier = () -> {
      try {
        return new PostgisTileStore(dataSource, mapper.read(this.tileset, Tileset.class));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };

    ServiceLocatorUtilities.bind(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(tileset).to(URI.class).named("tileset");
      }
    });

    BlockingStreamingHttpService httpService = new HttpJerseyRouterBuilder()
        .buildBlockingStreaming(new EditorApplication(new EditorModule(tileset, style, mapper, tileStoreSupplier)));
    ServerContext serverContext = HttpServers.forPort(port)
        .listenBlockingStreamingAndAwait(httpService);

    logger.info("Listening on {}", serverContext.listenAddress());

    // Blocks and awaits shutdown of the server this ServerContext represents.
    serverContext.awaitShutdown();

    return 0;
  }

}