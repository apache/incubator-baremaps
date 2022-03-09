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

package com.baremaps.config;

import com.baremaps.blob.Blob;
import com.baremaps.blob.BlobStore;
import com.baremaps.blob.BlobStoreException;
import java.io.IOException;
import java.net.URI;

/** A simple {@link Config} that reads and writes configuration files in a blob store. */
public class StaticConfig implements Config {

  private final BlobStore store;

  private final URI uri;

  /**
   * Constructs a static configuration.
   *
   * @param store the blob store on which the configuration is stored
   * @param uri the uri of the configuration file
   */
  public StaticConfig(BlobStore store, URI uri) {
    this.store = store;
    this.uri = uri;
  }

  /** {@inheritDoc} * */
  @Override
  public byte[] read() throws ConfigException {
    try {
      return store.get(uri).getInputStream().readAllBytes();
    } catch (IOException | BlobStoreException e) {
      throw new ConfigException(e);
    }
  }

  /** {@inheritDoc} * */
  @Override
  public void write(byte[] bytes) throws ConfigException {
    try {
      store.put(uri, Blob.builder().withByteArray(bytes).build());
    } catch (BlobStoreException e) {
      throw new ConfigException(e);
    }
  }
}
