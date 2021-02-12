package com.baremaps.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.blob.ResourceBlobStore;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class ConfigLoaderTest {

  @Test
  public void load() throws URISyntaxException, IOException {
    Config config = new ConfigLoader(new ResourceBlobStore()).load(new URI("res://./config.yaml"));
    assertEquals(config.getId(), "config");
    assertEquals(config.getLayers().size(), 1);
    assertEquals(config.getLayers().get(0).getId(), "layer");
    assertEquals(config.getStylesheets().size(), 1);
    assertEquals(config.getStylesheets().get(0).getId(), "stylesheet");
  }

}