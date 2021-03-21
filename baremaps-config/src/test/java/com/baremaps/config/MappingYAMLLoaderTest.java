package com.baremaps.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.blob.ResourceBlobStore;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class MappingYAMLLoaderTest {

  @Test
  public void load() throws URISyntaxException, IOException {
    Mapping mapping = new YamlStore(new ResourceBlobStore()).read(new URI("res://./mapping.yaml"), Mapping.class);
    assertEquals(mapping.getAllowTags().size(), 2);
    assertEquals(mapping.getAllowTags().get(0), "tag1");
    assertEquals(mapping.getAllowTags().get(1), "tag2");
  }

}