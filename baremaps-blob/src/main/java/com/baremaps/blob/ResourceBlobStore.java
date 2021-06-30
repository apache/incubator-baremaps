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

import com.google.common.io.Resources;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceBlobStore implements BlobStore {

  private static final String SCHEMA = "res://";

  private static Logger logger = LoggerFactory.getLogger(ResourceBlobStore.class);

  @Override
  public long size(URI uri) throws IOException {
    logger.debug("Size {}", uri);
    return Resources.asByteSource(Resources.getResource(path(uri))).size();
  }

  @Override
  public InputStream read(URI uri) throws IOException {
    logger.debug("Read {}", uri);
    return Resources.asByteSource(Resources.getResource(path(uri))).openStream();
  }

  @Override
  public byte[] readByteArray(URI uri) throws IOException {
    logger.debug("Read {}", uri);
    return Resources.toByteArray(Resources.getResource(path(uri)));
  }


  @Override
  public OutputStream write(URI uri) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeByteArray(URI uri, byte[] bytes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(URI uri) {
    throw new UnsupportedOperationException();
  }

  private String path(URI uri) {
    return uri.toString().replace(SCHEMA, "");
  }

}
