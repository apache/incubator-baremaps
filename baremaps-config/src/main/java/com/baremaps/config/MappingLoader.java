package com.baremaps.config;

import com.baremaps.blob.BlobStore;
import java.util.Map;

public class MappingLoader extends Loader<Mapping> {

  public MappingLoader(BlobStore blobStore) {
    super(blobStore, Mapping.class);
  }

  public MappingLoader(BlobStore blobStore, Map<String, String> variables) {
    super(blobStore, variables, Mapping.class);
  }

}
