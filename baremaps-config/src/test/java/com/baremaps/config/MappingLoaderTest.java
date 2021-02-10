package com.baremaps.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.blob.ResourceBlobStore;
import com.baremaps.config.mapping.Mapping;
import com.baremaps.config.mapping.MappingLoader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class MappingLoaderTest {

  @Test
  public void load() throws URISyntaxException, IOException {
    Mapping mapping = new MappingLoader(new ResourceBlobStore()).load(new URI("res://./mapping.yaml"));
    assertEquals(mapping.getAllowTags().size(), 2);
    assertEquals(mapping.getAllowTags().get(0), "tag1");
    assertEquals(mapping.getAllowTags().get(1), "tag2");
  }

}