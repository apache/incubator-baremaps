
package com.baremaps.cli;

import com.baremaps.config.BlobMapper;
import com.baremaps.config.style.Style;
import com.baremaps.config.tileset.Tileset;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "init", description = "Init the configuration files.")
public class Init implements Callable<Integer> {

  private static Logger logger = LoggerFactory.getLogger(Init.class);

  @Mixin
  private Options options;

  @Option(
      names = {"--tileset"},
      paramLabel = "TILESET",
      description = "The tileset file.")
  private URI tileset;

  @Option(
      names = {"--style"},
      paramLabel = "STYLE",
      description = "The style file.")
  private URI style;

  @Override
  public Integer call() throws IOException {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());

    BlobMapper mapper = new BlobMapper(options.blobStore());

    if (style != null && !mapper.exists(style)) {
      Style styleObject = new Style();
      mapper.write(style, styleObject);
    }

    if (tileset != null && !mapper.exists(tileset)) {
      Tileset tilesetObject = new Tileset();
      mapper.write(tileset, tilesetObject);
    }

    return 0;
  }

}