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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@code BlobStore} that routes to the another {@code BlobStore} based on the scheme specified in
 * the URI. By default, the router supports the file (file system) and res (class path) schemes. It
 * can be extended with the {@code addScheme} method.
 */
public class BlobStoreRouter implements BlobStore {

  public static final String UNSUPPORTED_SCHEME = "Unsupported scheme: %s";

  private final Map<String, BlobStore> schemes;

  /** Constructs a {@link BlobStoreRouter}. */
  public BlobStoreRouter() {
    schemes = new HashMap<>();
    schemes.put(null, new FileBlobStore());
    schemes.put("file", new FileBlobStore());
    schemes.put("res", new ResourceBlobStore());
  }

  /**
   * Adds a {@code BlobStore} implementation for the specified scheme.
   *
   * @param scheme the scheme (e.g. http, ftp, etc.)
   * @param blobStore the blob store
   * @return the router
   */
  public BlobStoreRouter addScheme(String scheme, BlobStore blobStore) {
    schemes.put(scheme, blobStore);
    return this;
  }

  /** {@inheritDoc} */
  public Blob head(URI uri) throws BlobStoreException {
    if (schemes.containsKey(uri.getScheme())) {
      return schemes.get(uri.getScheme()).head(uri);
    } else {
      throw new BlobStoreException(String.format(UNSUPPORTED_SCHEME, uri.getScheme()));
    }
  }

  /** {@inheritDoc} */
  @Override
  public Blob get(URI uri) throws BlobStoreException {
    if (schemes.containsKey(uri.getScheme())) {
      return schemes.get(uri.getScheme()).get(uri);
    } else {
      throw new BlobStoreException(String.format(UNSUPPORTED_SCHEME, uri.getScheme()));
    }
  }

  /** {@inheritDoc} */
  @Override
  public void put(URI uri, Blob blob) throws BlobStoreException {
    if (schemes.containsKey(uri.getScheme())) {
      schemes.get(uri.getScheme()).put(uri, blob);
    } else {
      throw new BlobStoreException(String.format(UNSUPPORTED_SCHEME, uri.getScheme()));
    }
  }

  /** {@inheritDoc} */
  @Override
  public void delete(URI uri) throws BlobStoreException {
    if (schemes.containsKey(uri.getScheme())) {
      schemes.get(uri.getScheme()).delete(uri);
    } else {
      throw new BlobStoreException(String.format(UNSUPPORTED_SCHEME, uri.getScheme()));
    }
  }
}
