
package com.baremaps.cli.command;

import static com.baremaps.cli.option.TileReaderOption.fast;

import com.baremaps.cli.handler.BlueprintHandler;
import com.baremaps.cli.handler.ConfigHandler;
import com.baremaps.cli.handler.FileHandler;
import com.baremaps.cli.handler.ResourceHandler;
import com.baremaps.cli.handler.StyleHandler;
import com.baremaps.cli.handler.TileHandler;
import com.baremaps.cli.option.TileReaderOption;
import com.baremaps.tiles.TileStore;
import com.baremaps.tiles.config.Config;
import com.baremaps.tiles.database.FastPostgisTileStore;
import com.baremaps.tiles.database.SlowPostgisTileStore;
import com.baremaps.util.fs.FileSystem;
import com.baremaps.util.postgis.PostgisHelper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Callable;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
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

  private HttpServer server;

  @Override
  public Integer call() throws IOException {
    Configurator.setRootLevel(Level.getLevel(mixins.logLevel.name()));
    logger.info("{} processors available.", Runtime.getRuntime().availableProcessors());

    startServer();

    Path assetsPath = Paths.get(assets.getPath()).toAbsolutePath();
    Path configPath = Paths.get(config.getPath()).toAbsolutePath();

    // Register a watch service in a separate thread to observe the changes occuring
    // in the assets directory and in the configuration file. If a change occurs,
    // the server is restarted, which triggers the browser to reload.
    if (watchChanges && Files.exists(assetsPath) && Files.exists(configPath)) {
      new Thread(() -> {
        try {
          WatchService watchService = FileSystems.getDefault().newWatchService();
          assetsPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
          configPath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
          WatchKey key;
          while ((key = watchService.take()) != null) {
            Path dir = (Path) key.watchable();
            for (WatchEvent<?> event : key.pollEvents()) {
              Path path = dir.resolve((Path) event.context());
              if (!path.endsWith("~")
                  && Files.exists(path)
                  && Files.getLastModifiedTime(path).toMillis() > lastChange) {
                lastChange = Files.getLastModifiedTime(path).toMillis();
                logger.info("Detected changes in the configuration.");
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
    FileSystem fileReader = mixins.fileSystem();
    try (InputStream input = fileReader.read(this.config)) {
      Yaml yaml = new Yaml(new Constructor(Config.class));
      Config config = yaml.load(input);

      logger.info("Initializing datasource.");
      PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);

      logger.info("Initializing tile reader.");
      TileStore tileStore = tileReader(datasource, config);

      logger.info("Initializing server.");
      server = HttpServer.create(new InetSocketAddress(config.getHost(), config.getPort()), 0);

      // Initialize the handlers
      server.createContext("/", new BlueprintHandler(config));
      server.createContext("/favicon.ico", new ResourceHandler("favicon.ico"));
      server.createContext("/config.yaml", new ConfigHandler(config));
      server.createContext("/style.json", new StyleHandler(config));
      server.createContext("/tiles/", new TileHandler(tileStore));
      server.createContext("/assets/", new FileHandler(Paths.get(assets.getPath())));

      // Keep a connection open with the browser.
      // When the server restarts, for instance when a change occurs in the configuration,
      // The browser reloads the webpage and displays the changes.
      server.createContext("/change/", exchange -> {
        if (!watchChanges) {
          exchange.sendResponseHeaders(204, -1);
        } else {
          logger.info("Waiting for changes.");
        }
      });

      server.setExecutor(null);

      logger.info("Start listening on port {}", config.getPort());
      server.start();

    } catch (Exception ex) {
      logger.error("A problem occured while starting the server.", ex);
    }
  }

  private void stopServer() throws IOException {
    logger.info("Stopping the server.");
    server.stop(0);
  }

  private TileStore tileReader(PoolingDataSource dataSource, com.baremaps.tiles.config.Config config) {
    switch (tileReader) {
      case slow:
        return new SlowPostgisTileStore(dataSource, config);
      case fast:
        return new FastPostgisTileStore(dataSource, config);
      default:
        throw new UnsupportedOperationException("Unsupported tile reader");
    }
  }


}