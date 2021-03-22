
package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.FileBlobStore;
import com.baremaps.config.Config;
import com.baremaps.config.YamlStore;
import com.baremaps.server.Server;
import io.micronaut.runtime.Micronaut;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "preview", description = "Preview the vector tiles.")
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
      description = "The configuration file.",
      required = true)
  private URI config;

  @Option(
      names = {"--style"},
      paramLabel = "STYLE",
      description = "The style file.",
      required = false)
  private URI style;

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

    logger.info("Initializing services");

    logger.info("Watch the configuration file for changes");
    Path watch = Paths.get(this.config.getPath()).toAbsolutePath().getParent();

    Micronaut.run(Server.class);

    logger.info("Start server");


    return 0;
  }

}