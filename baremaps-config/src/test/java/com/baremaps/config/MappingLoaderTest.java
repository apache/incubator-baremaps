package com.baremaps.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.blob.ResourceBlobStore;
import com.baremaps.config.mapping.Mapping;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class MappingLoaderTest {

  @Test
  void load() throws URISyntaxException, BlobMapperException {
    Mapping mapping = new BlobMapper(new ResourceBlobStore()).read(new URI("res://./mapping.json"), Mapping.class);
    assertEquals(2, mapping.getAllowTags().size());
    assertEquals("tag1", mapping.getAllowTags().get(0));
    assertEquals("tag2", mapping.getAllowTags().get(1));
  }

}