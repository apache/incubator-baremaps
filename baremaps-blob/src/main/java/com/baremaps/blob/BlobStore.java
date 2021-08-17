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

/** An abstraction to read and write blobs identified by URIs. */
public interface BlobStore {

  Blob head(URI uri) throws BlobStoreException;

  Blob get(URI uri) throws BlobStoreException;

  void put(URI uri, Blob blob) throws BlobStoreException;

  void delete(URI uri) throws BlobStoreException;
}
