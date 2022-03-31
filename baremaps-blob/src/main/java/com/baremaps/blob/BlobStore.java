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

/**
 * Provides an interface to a blob store. Allows to read and write blobs identified by Uniform
 * Resource Identifier (URI) reference.
 */
public interface BlobStore {

  /**
   * Returns a blob without its content (header information only).
   *
   * @param uri the URI
   * @return the blob
   * @throws BlobStoreException a blob store exception
   */
  Blob head(URI uri) throws BlobStoreException;

  /**
   * Returns a blob.
   *
   * @param uri the URI
   * @return the blob
   * @throws BlobStoreException a blob store exception
   */
  Blob get(URI uri) throws BlobStoreException;

  /**
   * Puts a blob at a specified URI.
   *
   * @param uri the URI
   * @param blob the blob
   * @throws BlobStoreException a blob store exception
   */
  void put(URI uri, Blob blob) throws BlobStoreException;

  /**
   * Deletes a blob at a specified URI.
   *
   * @param uri the URI
   * @throws BlobStoreException a blob store exception
   */
  void delete(URI uri) throws BlobStoreException;
}
