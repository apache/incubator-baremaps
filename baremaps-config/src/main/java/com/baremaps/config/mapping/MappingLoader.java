package com.baremaps.config.mapping;

import com.baremaps.blob.BlobStore;
import com.baremaps.config.ConfigLoader;

public class MappingLoader extends ConfigLoader {

  public MappingLoader(BlobStore blobStore) {
    super(blobStore, Mapping.class);
  }

}
