package com.baremaps.config.style;

import com.baremaps.blob.BlobStore;
import com.baremaps.config.ConfigLoader;

public class StyleLoader extends ConfigLoader<Style> {

  public StyleLoader(BlobStore blobStore) {
    super(blobStore, Style.class, StyleSheet.class);
  }

}
