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

/** A helper for fetching blobs. */
public class BlobFetcher {

  private final BlobStore blobStore;

  /**
   * Constructs a {@code DownloadManager} for the specified {@code BlobStore}.
   *
   * @param blobStore the blob store
   */
  public BlobFetcher(BlobStore blobStore) {
    this.blobStore = blobStore;
  }

  /**
   * Fetches a blob and return its path on the file system. A temporary file may be created to store
   * the content of the blob.
   *
   * @param blob the URI of the blob
   * @return the path of the fetched blob
   * @throws BlobStoreException a blob store exception
   */
  public Path fetch(URI blob) throws BlobStoreException {
    if (blob.getScheme() == null || blob.getScheme().equals("file")) {
      return Paths.get(blob.getPath());
    }
    try {
      File file = File.createTempFile("download_", ".blob", Paths.get(".").toFile());
      file.deleteOnExit();
      Path path = file.toPath().toAbsolutePath();
      return fetch(blob, path);
    } catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }

  /**
   * Fetches a blob if it changed and returns its path on the file system.
   *
   * @param blob the URI of the blob
   * @param hint a hint on where to store the blob
   * @return the path of the fetched blob
   * @throws BlobStoreException
   */
  public Path fetchIfChanged(URI blob, Path hint) throws BlobStoreException {
    if (blob.getScheme() == null || blob.getScheme().equals("file")) {
      return Paths.get(blob.getPath());
    }
    try {
      Long oldSize = Files.size(hint);
      Long newSize = blobStore.head(blob).getContentLength();
      if (oldSize == newSize) {
        return hint;
      }
      return fetch(blob, hint);
    } catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }

  /**
   * Fetches a blob and returns its path on the file system.
   *
   * @param blob the URI of the blob
   * @param hint a hint on where to store the blob
   * @return the path of the downloaded blob
   * @throws BlobStoreException a blob store exception
   */
  public Path fetch(URI blob, Path hint) throws BlobStoreException {
    if (blob.getScheme() == null || blob.getScheme().equals("file")) {
      return Paths.get(blob.getPath());
    }
    try (InputStream input = blobStore.get(blob).getInputStream()) {
      Files.copy(input, hint, StandardCopyOption.REPLACE_EXISTING);
      return hint;
    } catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }
}
