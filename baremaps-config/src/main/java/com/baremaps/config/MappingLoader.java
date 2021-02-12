package com.baremaps.config;

import com.baremaps.blob.BlobStore;

public class MappingLoader extends Loader<Mapping> {

  public MappingLoader(BlobStore blobStore) {
    super(blobStore, Mapping.class);
  }

}
