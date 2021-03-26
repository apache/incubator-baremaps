package com.baremaps.server;

import com.baremaps.config.Config;
import com.baremaps.server.transfer.Tileset;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.ProducesJson;
import com.linecorp.armeria.server.annotation.ProducesText;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigService {

  private static Logger logger = LoggerFactory.getLogger(ConfigService.class);

  private final Path config;

  public ConfigService(Path config) {
    this.config = config;
  }

  @Get("/config.json")
  @ProducesJson
  public Config configJson() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.setSerializationInclusion(Include.NON_NULL);
    return mapper.readValue(Files.readString(config), Config.class);
  }

  @Get("/config.yaml")
  @ProducesText
  public String configYaml() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.setSerializationInclusion(Include.NON_NULL);
    Config object = mapper.readValue(Files.readString(config), Config.class);
    return mapper.writeValueAsString(object);
  }

  @Get("/tiles.json")
  @ProducesJson
  public Object tilesJson() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.setSerializationInclusion(Include.NON_NULL);
    Config object = mapper.readValue(Files.readString(config), Config.class);
    return new Tileset().toTileset(object);
  }


}
