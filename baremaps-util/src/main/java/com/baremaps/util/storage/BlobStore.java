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
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BlobStore {

  private static Logger logger = LogManager.getLogger();

  private final Map<URI, Path> cache = new HashMap<>();

  protected Path cache(URI uri) throws IOException {
    if (!cache.containsKey(uri)) {
      String fileName = Paths.get(uri.getPath()).getFileName().toString();
      File tmpFile = File.createTempFile("baremaps_", "_" + fileName);
      logger.debug("Cache {} in {}", uri, tmpFile);
      try (InputStream input = read(uri)) {
        tmpFile.deleteOnExit();
        copy(input, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        cache.put(uri, tmpFile.toPath());
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
