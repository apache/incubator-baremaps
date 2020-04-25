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

package com.baremaps.util.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class CachedFileSystem extends FileSystem {

  private final Map<URI, Path> cache = new HashMap<>();

  private final FileSystem fileSystem;

  public CachedFileSystem(FileSystem FileSystem) {
    this.fileSystem = FileSystem;
  }

  @Override
  public boolean accept(URI uri) {
    return fileSystem.accept(uri);
  }

  @Override
  public InputStream read(URI uri) throws IOException {
    if (!cache.containsKey(uri)) {
      try (InputStream input = fileSystem.read(uri)) {
        File temp = File.createTempFile("baremaps_", ".tmp");
        temp.deleteOnExit();
        Files.copy(input, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        cache.put(uri, temp.toPath());
      }
    }
    return Files.newInputStream(cache.get(uri));
  }

  @Override
  public OutputStream write(URI uri) throws IOException {
    cache.remove(uri);
    return fileSystem.write(uri);
  }

  @Override
  public void delete(URI uri) throws IOException {
    cache.remove(uri);
    fileSystem.delete(uri);
  }

}
