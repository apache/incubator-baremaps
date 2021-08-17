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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileBlobStore implements BlobStore {

  private Path file(URI uri) {
    return Paths.get(uri.getPath()).toAbsolutePath();
  }

  @Override
  public Blob head(URI uri) throws BlobStoreException {
    try {
      Path file = file(uri);
      return Blob.builder().withContentLength(Files.size(file)).build();
    } catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }

  @Override
  public Blob get(URI uri) throws BlobStoreException {
    try {
      Path file = file(uri);
      return Blob.builder()
          .withContentLength(Files.size(file))
          .withInputStream(Files.newInputStream(file))
          .build();
    } catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }

  @Override
  public void put(URI uri, Blob blob) throws BlobStoreException {
    try {
      Path file = file(uri);
      if (!Files.exists(file.getParent())) {
        Files.createDirectories(file.getParent());
      }
      Files.copy(blob.getInputStream(), file, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }

  @Override
  public void delete(URI uri) throws BlobStoreException {
    try {
      Path file = file(uri);
      Files.deleteIfExists(file);
    } catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }
}
