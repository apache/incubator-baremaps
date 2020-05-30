
package com.baremaps.cli.command;

import static com.baremaps.cli.option.TileReaderOption.fast;

import com.baremaps.cli.handler.BlueprintHandler;
import com.baremaps.cli.handler.ConfigHandler;
import com.baremaps.cli.handler.StyleHandler;
import com.baremaps.cli.handler.TileHandler;
import com.baremaps.cli.option.TileReaderOption;
import com.baremaps.tiles.TileStore;
import com.baremaps.tiles.config.Config;
import com.baremaps.tiles.database.FastPostgisTileStore;
import com.baremaps.tiles.database.SlowPostgisTileStore;
import com.baremaps.util.fs.FileSystem;
import com.baremaps.util.postgis.PostgisHelper;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.file.FileService;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.concurrent.Callable;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "serve", description = "Serve vector tiles from the the Postgresql database.")
public class Serve implements Callable<Integer> {

  private static Logger logger = LogManager.getLogger();

  @Mixin
  private Mixins mixins;

  @Option(
      names = {"--database"},
      paramLabel = "JDBC",
      description = "The JDBC url of the Postgres database.",
      required = true)
  private String database;

  @Option(
      names = {"--config"},
      paramLabel = "YAML",
      description = "The YAML configuration file.",
      required = true)
  private URI config;

  @Option(
      names = {"--assets"},
      paramLabel = "ASSETS",
      description = "A directory containing assets.")
  private URI assets;

  @Option(
      names = {"--reader"},
      paramLabel = "READER",
      description = "The tile reader.")
  private TileReaderOption tileReader = fast;

  @Option(
      names = {"--watch-changes"},
      paramLabel = "WATCH_CHANGES",
      description = "Watch for file changes.")
  private boolean watchChanges = false;

  private long lastChange = 0;

  private Server server;

  @Override
  public Integer call() throws IOException {
    Configurator.setRootLevel(Level.getLevel(mixins.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());

    startServer();

    Path configPath = Paths.get(config.getPath()).toAbsolutePath();

    // Register a watch service in a separate thread to observe the changes occuring
    // in the assets directory and in the configuration file. If a change occurs,
    // the server is restarted, which triggers the browser to reload.
    if (watchChanges && Files.exists(configPath)) {
      new Thread(() -> {
        try {
          WatchService watchService = FileSystems.getDefault().newWatchService();
          configPath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

          // Watch the optional assets directory
          if (assets != null) {
            Path assetsPath = Paths.get(assets.getPath()).toAbsolutePath();
            assetsPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
          }

          WatchKey key;
          while ((key = watchService.take()) != null) {
            Path dir = (Path) key.watchable();
            for (WatchEvent<?> event : key.pollEvents()) {
              Path path = dir.resolve((Path) event.context());
              if (!path.endsWith("~")
                  && Files.exists(path)
                  && Files.getLastModifiedTime(path).toMillis() > lastChange) {
                lastChange = Files.getLastModifiedTime(path).toMillis();
                logger.info("Detected changes in the configuration");
                stopServer();
                startServer();
              }
            }
            key.reset();
          }
        } catch (InterruptedException e) {
          logger.error(e);
        } catch (IOException e) {
          logger.error(e);
        }
      }).run();
    }

    return 0;
  }

  private void startServer() throws IOException {
    FileSystem fileReader = mixins.filesystem();
    try (InputStream input = fileReader.read(this.config)) {
      Config config = Config.load(input);

      logger.info("Initializing datasource");
      PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);

      logger.info("Initializing tile reader");
      final TileStore tileStore;
      switch (tileReader) {
        case slow:
          tileStore = new SlowPostgisTileStore(datasource, config);
          break;
        case fast:
          tileStore = new FastPostgisTileStore(datasource, config);
          break;
        default:
          throw new UnsupportedOperationException("Unsupported tile reader");
      }

      logger.info("Initializing server");

      String host = config.getServer().getHost();
      int port = config.getServer().getPort();
      ServerBuilder builder = Server.builder()
          .defaultHostname(host)
          .http(port)
          .service("/", new BlueprintHandler(config))
          .service("/favicon.ico",
              FileService.of(ClassLoader.getSystemClassLoader(), "/favicon.ico"))
          .service("/config.yaml", new ConfigHandler(config))
          .service("/style.json", new StyleHandler(config))
          .service("regex:^/tiles/(?<z>[0-9]+)/(?<x>[0-9]+)/(?<y>[0-9]+).pbf$",
              new TileHandler(tileStore));

      // Initialize the assets handler if a path has been provided
      if (assets != null) {
        builder.service("/assets/", FileService.of(Paths.get(assets.getPath())));
      }

      // Keep a connection open with the browser.
      // When the server restarts, for instance when a change occurs in the configuration,
      // The browser reloads the webpage and displays the changes.
      builder.service("/change/", (ctx, req) -> {
        if (!watchChanges) {
          return HttpResponse.of(200);
        } else {
          logger.info("Waiting for changes");
          return HttpResponse.streaming();
        }
      });

      server = builder.build();
      server.start();

    } catch (Exception ex) {
      logger.error("A problem occured while starting the server", ex);
    }
  }

  private void stopServer() throws IOException {
    logger.info("Stopping the server");
    server.stop();
  }

}