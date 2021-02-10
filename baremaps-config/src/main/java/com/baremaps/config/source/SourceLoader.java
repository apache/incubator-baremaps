package com.baremaps.config.source;

import com.baremaps.blob.BlobStore;
import com.baremaps.config.ConfigLoader;

public class SourceLoader extends ConfigLoader<Source> {

  public SourceLoader(BlobStore blobStore) {
    super(blobStore, Source.class, SourceLayer.class);
  }

}
