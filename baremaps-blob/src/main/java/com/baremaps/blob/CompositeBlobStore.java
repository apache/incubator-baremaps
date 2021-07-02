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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class CompositeBlobStore implements BlobStore {

  public static final String UNSUPPORTED_SCHEME = "Unsupported scheme: %s";

  private final Map<String, BlobStore> schemes;

  public CompositeBlobStore() {
    schemes = new HashMap<>();
    schemes.put(null, new FileBlobStore());
    schemes.put("file", new FileBlobStore());
    schemes.put("res", new ResourceBlobStore());
  }

  public void addScheme(String scheme, BlobStore blobStore) {
    schemes.put(scheme, blobStore);
  }

  @Override
  public long size(URI uri) throws IOException {
    if (schemes.containsKey(uri.getScheme())) {
      return schemes.get(uri.getScheme()).size(uri);
    } else {
      throw new IOException(String.format(UNSUPPORTED_SCHEME, uri.getScheme()));
    }
  }

  @Override
  public InputStream read(URI uri) throws IOException {
    if (schemes.containsKey(uri.getScheme())) {
      return schemes.get(uri.getScheme()).read(uri);
    } else {
      throw new IOException(String.format(UNSUPPORTED_SCHEME, uri.getScheme()));
    }
  }

  @Override
  public byte[] readByteArray(URI uri) throws IOException {
    if (schemes.containsKey(uri.getScheme())) {
      return schemes.get(uri.getScheme()).readByteArray(uri);
    } else {
      throw new IOException(String.format(UNSUPPORTED_SCHEME, uri.getScheme()));
    }
  }

  @Override
  public OutputStream write(URI uri) throws IOException {
    if (schemes.containsKey(uri.getScheme())) {
      return schemes.get(uri.getScheme()).write(uri);
    } else {
      throw new IOException(String.format(UNSUPPORTED_SCHEME, uri.getScheme()));
    }
  }

  @Override
  public OutputStream write(URI uri, Map<String, String> metadata) throws IOException {
    if (schemes.containsKey(uri.getScheme())) {
      return schemes.get(uri.getScheme()).write(uri, metadata);
    } else {
      throw new IOException(String.format(UNSUPPORTED_SCHEME, uri.getScheme()));
    }
  }

  @Override
  public void writeByteArray(URI uri, byte[] bytes) throws IOException {
    if (schemes.containsKey(uri.getScheme())) {
      schemes.get(uri.getScheme()).writeByteArray(uri, bytes);
    } else {
      throw new IOException(String.format(UNSUPPORTED_SCHEME, uri.getScheme()));
    }
  }

  @Override
  public void writeByteArray(URI uri, byte[] bytes, Map<String, String> metadata) throws IOException {
    if (schemes.containsKey(uri.getScheme())) {
      schemes.get(uri.getScheme()).writeByteArray(uri, bytes, metadata);
    } else {
      throw new IOException(String.format(UNSUPPORTED_SCHEME, uri.getScheme()));
    }
  }

  @Override
  public void delete(URI uri) throws IOException {
    if (schemes.containsKey(uri.getScheme())) {
      schemes.get(uri.getScheme()).delete(uri);
    } else {
      throw new IOException(String.format(UNSUPPORTED_SCHEME, uri.getScheme()));
    }
  }

}
