package com.baremaps.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.blob.ResourceBlobStore;
import com.baremaps.config.style.Style;
import com.baremaps.config.tileset.Tileset;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class BlobMapperTest {

  @Test
  public void loadJsonTileset() throws URISyntaxException, IOException {
    Tileset tileset = new BlobMapper(new ResourceBlobStore()).read(new URI("res://./tileset.json"), Tileset.class);
    assertEquals(tileset.getLayers().size(), 1);
    assertEquals(tileset.getLayers().get(0).getId(), "layer");
  }

  @Test
  public void loadJsonStyle() throws URISyntaxException, IOException {
    Style style = new BlobMapper(new ResourceBlobStore()).read(new URI("res://./style.json"), Style.class);
    assertEquals(style.getName(), "style");
  }

  @Test
  public void loadYamlStyle() throws URISyntaxException, IOException {
    Style style = new BlobMapper(new ResourceBlobStore()).read(new URI("res://./style.yaml"), Style.class);
    assertEquals(style.getName(), "style");
  }

}