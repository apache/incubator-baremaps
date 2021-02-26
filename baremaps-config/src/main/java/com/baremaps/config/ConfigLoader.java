package com.baremaps.config;

import com.baremaps.blob.BlobStore;
import java.util.Map;

public class ConfigLoader extends Loader<Config> {

  public ConfigLoader(BlobStore blobStore) {
    super(blobStore, Config.class, Layer.class, Stylesheet.class);
  }

  public ConfigLoader(BlobStore blobStore, Map<String, String> variables) {
    super(blobStore, variables, Config.class, Layer.class, Stylesheet.class);
  }

}
