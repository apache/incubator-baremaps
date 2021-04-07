package com.baremaps.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.blob.ResourceBlobStore;
import com.baremaps.config.tileset.Tileset;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfigYAMLLoaderTest {

  @Test
  public void load() throws URISyntaxException, IOException {
    Map<String, String> variables = ImmutableMap.of("host", "localhost", "port", "9000");
    Tileset tileset = new BlobMapper(new ResourceBlobStore(), variables).read(new URI("res://./tileset.yaml"), Tileset.class);
    assertEquals(tileset.getLayers().size(), 1);
    assertEquals(tileset.getLayers().get(0).getId(), "layer");
  }

}