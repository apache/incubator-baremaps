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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

/** A {@code BlobStore} for reading and writing blobs over HTTP. */
public class HttpBlobStore implements BlobStore {

  /** {@inheritDoc} */
  @Override
  public Blob head(URI uri) throws BlobStoreException {
    try {
      HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
      conn.setRequestMethod("HEAD");
      return Blob.builder()
          .withContentLength(conn.getContentLengthLong())
          .withContentType(conn.getContentType())
          .build();
    } catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Blob get(URI uri) throws BlobStoreException {
    try {
      HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
      conn.setRequestMethod("GET");
      return Blob.builder()
          .withContentLength(conn.getContentLengthLong())
          .withContentType(conn.getContentType())
          .withInputStream(new BufferedInputStream(conn.getInputStream()))
          .build();
    } catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void put(URI uri, Blob blob) throws BlobStoreException {
    try {
      HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
      conn.setRequestMethod("PUT");
      conn.setRequestProperty("Content-Length", String.valueOf(blob.getContentLength()));
      conn.setRequestProperty("Content-Type", String.valueOf(blob.getContentType()));
      conn.setRequestProperty("Content-Encoding", String.valueOf(blob.getContentEncoding()));
      try (OutputStream outputStream = new BufferedOutputStream(conn.getOutputStream())) {
        blob.getInputStream().transferTo(outputStream);
      }
    } catch (IOException exception) {
      throw new BlobStoreException(exception);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void delete(URI uri) throws BlobStoreException {
    try {
      HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
      conn.setRequestMethod("DELETE");
      conn.getInputStream();
    } catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }
}
