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

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URI;

public class ResourceBlobStore implements BlobStore {

  private static final String SCHEMA = "res://";

  @Override
  public Blob head(URI uri) throws BlobStoreException {
    try {
      ByteSource byteSource = byteSource(uri);
      return Blob.builder()
          .withContentLength(byteSource.size())
          .build();
    } catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }

  @Override
  public Blob get(URI uri) throws BlobStoreException {
    try {
      ByteSource byteSource = byteSource(uri);
      return Blob.builder()
          .withContentLength(byteSource.size())
          .withInputStream(byteSource.openBufferedStream())
          .build();
    } catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }

  @Override
  public void put(URI uri, Blob blob) throws BlobStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(URI uri) throws BlobStoreException {
    throw new UnsupportedOperationException();
  }

  private ByteSource byteSource(URI uri) {
    return Resources.asByteSource(Resources.getResource(uri.toString().replace(SCHEMA, "")));
  }

}
