package com.baremaps.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.blob.ResourceBlobStore;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfigLoaderTest {

  @Test
  public void load() throws URISyntaxException, IOException {
    Map<String, String> variables = ImmutableMap.of("host", "localhost", "port", "9000");
    Config config = new ConfigLoader(new ResourceBlobStore(), variables).load(new URI("res://./config.yaml"));
    assertEquals(config.getId(), "config");
    assertEquals(config.getServer().getHost(), "localhost");
    assertEquals(config.getServer().getPort(), 9000);
    assertEquals(config.getLayers().size(), 1);
    assertEquals(config.getLayers().get(0).getId(), "layer");
    assertEquals(config.getStylesheets().size(), 1);
    assertEquals(config.getStylesheets().get(0).getId(), "stylesheet");
  }

}