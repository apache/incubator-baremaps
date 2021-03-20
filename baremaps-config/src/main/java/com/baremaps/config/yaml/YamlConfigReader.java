package com.baremaps.config.yaml;

import com.baremaps.blob.BlobStore;
import com.baremaps.config.Config;
import com.baremaps.config.Layer;
import java.util.Map;

public class YamlConfigReader extends YamlReader<Config> {

  public YamlConfigReader(BlobStore blobStore) {
    super(blobStore, Config.class, Layer.class);
  }

  public YamlConfigReader(BlobStore blobStore, Map<String, String> variables) {
    super(blobStore, variables, Config.class, Layer.class);
  }

}
