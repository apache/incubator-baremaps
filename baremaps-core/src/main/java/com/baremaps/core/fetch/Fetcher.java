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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * A helper for fetching and possibly caching data in temporary files.
 */
public class Fetcher {

  private final AmazonS3 client;

  private final boolean enableCaching;

  public Fetcher() {
    this(AmazonS3ClientBuilder.defaultClient(), false);
  }

  public Fetcher(boolean enableCaching) {
    this(AmazonS3ClientBuilder.defaultClient(), enableCaching);
  }

  public Fetcher(AmazonS3 client, boolean enableCaching) {
    this.client = client;
    this.enableCaching = enableCaching;
  }

  /**
   * Fetch data according to a path.
   *
   * @param path
   * @return
   * @throws IOException
   */
  public Data fetch(String path) throws IOException {
    if (Files.exists(Paths.get(path))) {
      return new FileData(Paths.get(path));
    } else {
      Data data = data(path);
      if (enableCaching) data = cache(data);
      return data;
    }
  }

  protected Data data(String path) throws IOException {
    try {
      URI uri = new URI(path);
      switch (uri.getScheme()) {
        case "http":
        case "https":
          return new URLData(uri.toURL());
        case "s3":
          return new S3Data(client, uri);
        default:
          throw new IOException(String.format("Invalid URI: %s", uri));
      }
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  protected Data cache(Data data) throws IOException {
    try (InputStream input = data.getInputStream()) {
      File tmp = File.createTempFile("baremaps_", ".tmp");
      tmp.deleteOnExit();
      Files.copy(input, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
      return new FileData(tmp.toPath());
    }
  }

}
