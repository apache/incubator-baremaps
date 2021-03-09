package com.baremaps.config.yaml;

import com.baremaps.blob.BlobStore;
import com.baremaps.config.Mapping;
import java.util.Map;

public class YamlMappingReader extends YamlReader<Mapping> {

  public YamlMappingReader(BlobStore blobStore) {
    super(blobStore, Mapping.class);
  }

  public YamlMappingReader(BlobStore blobStore, Map<String, String> variables) {
    super(blobStore, variables, Mapping.class);
  }

}
