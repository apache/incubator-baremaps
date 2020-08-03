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

package com.baremaps.util.storage;

import static java.nio.file.Files.copy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public abstract class BlobStore {

  private final Map<URI, Path> cache = new HashMap<>();

  protected Path cache(URI uri) throws IOException {
    if (!cache.containsKey(uri)) {
      try (InputStream input = read(uri)) {
        File temp = File.createTempFile("baremaps_", ".tmp");
        temp.deleteOnExit();
        copy(input, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        cache.put(uri, temp.toPath());
      }
    }
    return cache.get(uri);
  }

  protected abstract boolean accept(URI uri);

  public abstract Path fetch(URI uri) throws IOException;

  public abstract InputStream read(URI uri) throws IOException;

  public abstract byte[] readByteArray(URI uri) throws IOException;

  public abstract OutputStream write(URI uri) throws IOException;

  public abstract void writeByteArray(URI uri, byte[] bytes) throws IOException;

  public abstract void delete(URI uri) throws IOException;

}
