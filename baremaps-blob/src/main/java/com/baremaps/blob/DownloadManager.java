/*
 * Copyright (C) 2020 The Baremaps Authors
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

package com.baremaps.blob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DownloadManager {

  private final BlobStore blobStore;

  public DownloadManager(BlobStore blobStore) {
    this.blobStore = blobStore;
  }

  public Path download(URI uri) throws BlobStoreException {
    if (uri.getScheme() == null || uri.getScheme().equals("file")) {
      return Paths.get(uri.getPath());
    } else {
      try {
        File tempFile = File.createTempFile("baremaps_", ".tmp");
        tempFile.deleteOnExit();
        try (InputStream input = blobStore.get(uri).getInputStream()) {
          Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile.toPath();
      } catch (IOException e) {
        throw new BlobStoreException(e);
      }
    }
  }
}
