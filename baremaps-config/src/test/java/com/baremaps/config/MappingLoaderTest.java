package com.baremaps.config;

import static org.junit.jupiter.api.Assertions.*;

import com.baremaps.blob.ResourceBlobStore;
import com.baremaps.config.style.Style;
import com.baremaps.config.style.StyleLoader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class MappingLoaderTest {

  @Test
  public void load() throws URISyntaxException, IOException {
    Style style = new StyleLoader(new ResourceBlobStore()).load(new URI("res://./mapping.yaml"));
    assertEquals(style.getId(), "style");
    assertEquals(style.getSheets().size(), 2);
    assertEquals(style.getSheets().get(0).getId(), "sheet");
    assertEquals(style.getSheets().get(1).getId(), "sheet");
    assertEquals(style.getSheets().get(1).getLayers().get(0).getId(), "layer");
    assertEquals(style.getSheets().get(1).getLayers().get(0).getSourceLayer(), "source-layer");
  }


}