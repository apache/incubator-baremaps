package com.baremaps.config;

import com.baremaps.blob.BlobStore;

public class ConfigLoader extends Loader<Config> {

  public ConfigLoader(BlobStore blobStore) {
    super(blobStore, Config.class, Layer.class, Stylesheet.class);
  }

}
