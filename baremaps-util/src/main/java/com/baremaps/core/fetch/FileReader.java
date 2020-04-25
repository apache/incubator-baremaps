/*
 * Copyright (C) 2011 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.core.fetch;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

/**
 * A helper for fetching and possibly caching data in temporary files.
 */
public class FileReader {

  private final S3Client client;

  private final boolean enableCaching;

  private final Map<String, Path> cache = new HashMap<>();

  public FileReader() {
    this(client(), false);
  }

  public FileReader(boolean enableCaching) {
    this(client(), enableCaching);
  }

  public FileReader(S3Client client, boolean enableCaching) {
    this.client = client;
    this.enableCaching = enableCaching;
  }

  private static S3Client client() {
    try {
      return S3Client.builder().region(Region.US_WEST_2).build();
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * Fetch data according to a path.
   *
   * @param path
   * @return
   * @throws IOException
   */
  public InputStream read(String path) throws IOException {
    if (enableCaching) {
      if (!cache.containsKey(path)) {
        try (InputStream input = data(path)) {
          File tmp = File.createTempFile("baremaps_", ".tmp");
          tmp.deleteOnExit();
          Files.copy(input, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
          cache.put(path, tmp.toPath());
        }
      }
      return Files.newInputStream(cache.get(path));
    } else {
      return data(path);
    }
  }

  private InputStream data(String path) throws IOException {
    if (Files.exists(Paths.get(path))) {
      return Files.newInputStream(Paths.get(path));
    } else {
      try {
        URI uri = new URI(path);
        switch (uri.getScheme()) {
          case "http":
          case "https":
            return new BufferedInputStream(uri.toURL().openConnection().getInputStream());
          case "s3":
            String bucket = uri.getHost();
            String key = uri.getPath().substring(1);
            GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key).build();
            return client.getObject(request, ResponseTransformer.toInputStream());
          default:
            throw new IOException(String.format("Unsupported scheme: %s", uri.getScheme()));
        }
      } catch (URISyntaxException ex) {
        throw new IOException(ex);
      }
    }
  }

}
