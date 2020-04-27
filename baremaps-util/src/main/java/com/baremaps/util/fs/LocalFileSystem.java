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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalFileSystem extends FileSystem {

  @Override
  public boolean accept(URI uri) {
    return uri.getScheme() == null
        && uri.getHost() == null
        && uri.getPath() != null;
  }

  @Override
  public InputStream read(URI uri) throws IOException {
    return Files.newInputStream(Paths.get(uri.getPath()));
  }

  @Override
  public byte[] readByteArray(URI uri) throws IOException {
    return Files.readAllBytes(Paths.get(uri.getPath()));
  }

  @Override
  public OutputStream write(URI uri) throws IOException {
    Path path = Paths.get(uri.getPath());
    if (!Files.exists(path.getParent())) {
      Files.createDirectories(path.getParent());
    }
    return Files.newOutputStream(Paths.get(uri.getPath()));
  }

  @Override
  public void writeByteArray(URI uri, byte[] bytes) throws IOException {
    Path path = Paths.get(uri.getPath());
    if (!Files.exists(path.getParent())) {
      Files.createDirectories(path.getParent());
    }
    Files.write(path, bytes);
  }

  @Override
  public void delete(URI uri) throws IOException {
    Files.delete(Paths.get(uri.getPath()));
  }

}
