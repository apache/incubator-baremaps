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

package com.baremaps.core.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CompositeBlobStore extends BlobStore {

  private final List<BlobStore> components;

  public CompositeBlobStore(BlobStore... components) {
    this(Arrays.asList(components));
  }

  public CompositeBlobStore(List<BlobStore> components) {
    this.components = components;
  }

  private Optional<BlobStore> findFileSystem(URI uri) {
    return components.stream().filter(fileStore -> fileStore.accept(uri)).findFirst();
  }

  @Override
  public boolean accept(URI uri) {
    return findFileSystem(uri).isPresent();
  }

  @Override
  public Path fetch(URI uri) throws IOException {
    Optional<BlobStore> fileStore = findFileSystem(uri);
    if (fileStore.isPresent()) {
      return fileStore.get().fetch(uri);
    } else {
      throw new IOException("Unsupported URI: " + uri);
    }
  }

  @Override
  public InputStream read(URI uri) throws IOException {
    Optional<BlobStore> fileStore = findFileSystem(uri);
    if (fileStore.isPresent()) {
      return fileStore.get().read(uri);
    } else {
      throw new IOException("Unsupported URI: " + uri);
    }
  }

  @Override
  public byte[] readByteArray(URI uri) throws IOException {
    Optional<BlobStore> fileStore = findFileSystem(uri);
    if (fileStore.isPresent()) {
      return fileStore.get().readByteArray(uri);
    } else {
      throw new IOException("Unsupported URI: " + uri);
    }
  }

  @Override
  public OutputStream write(URI uri) throws IOException {
    Optional<BlobStore> fileSystem = findFileSystem(uri);
    if (fileSystem.isPresent()) {
      return fileSystem.get().write(uri);
    } else {
      throw new IOException("Unsupported URI: " + uri);
    }
  }

  @Override
  public void writeByteArray(URI uri, byte[] bytes) throws IOException {
    Optional<BlobStore> fileSystem = findFileSystem(uri);
    if (fileSystem.isPresent()) {
      fileSystem.get().writeByteArray(uri, bytes);
    } else {
      throw new IOException("Unsupported URI: " + uri);
    }
  }

  @Override
  public void delete(URI uri) throws IOException {
    Optional<BlobStore> fileStore = findFileSystem(uri);
    if (fileStore.isPresent()) {
      fileStore.get().delete(uri);
    } else {
      throw new IOException("Unsupported URI: " + uri);
    }
  }

}
