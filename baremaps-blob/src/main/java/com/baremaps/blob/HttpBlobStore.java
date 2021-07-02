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

import com.google.common.io.ByteStreams;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpBlobStore implements BlobStore {

  private static Logger logger = LoggerFactory.getLogger(HttpBlobStore.class);

  public HttpBlobStore() {

  }

  @Override
  public long size(URI uri) throws IOException {
    logger.debug("Size {}", uri);
    HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
    conn.setRequestMethod("HEAD");
    return conn.getContentLengthLong();
  }

  @Override
  public InputStream read(URI uri) throws IOException {
    logger.debug("Read {}", uri);
    HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
    conn.setRequestMethod("GET");
    return new BufferedInputStream(conn.getInputStream());
  }

  @Override
  public byte[] readByteArray(URI uri) throws IOException {
    logger.debug("Read {}", uri);
    HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
    conn.setRequestMethod("GET");
    try (InputStream inputStream = conn.getInputStream()) {
      return ByteStreams.toByteArray(inputStream);
    }
  }

  @Override
  public OutputStream write(URI uri) throws IOException {
    return write(uri, Map.of());
  }

  @Override
  public OutputStream write(URI uri, Map<String, String> metadata) throws IOException {
    logger.debug("Write {}", uri);
    return new ByteArrayOutputStream() {
      @Override
      public void close() throws IOException {
        byte[] bytes = this.toByteArray();
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("PUT");
        for (Entry<String, String> entry : metadata.entrySet()) {
          conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
        conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
        try (OutputStream outputStream = new BufferedOutputStream(conn.getOutputStream());
            InputStream inputStream = new ByteArrayInputStream(bytes)) {
          ByteStreams.copy(inputStream, outputStream);
        }
      }
    };
  }

  @Override
  public void writeByteArray(URI uri, byte[] bytes) throws IOException {
    writeByteArray(uri, bytes, Map.of());
  }

  @Override
  public void writeByteArray(URI uri, byte[] bytes, Map<String, String> metadata) throws IOException {
    logger.debug("Write {}", uri);
    HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
    conn.setRequestMethod("PUT");
    for (Entry<String, String> entry : metadata.entrySet()) {
      conn.setRequestProperty(entry.getKey(), entry.getValue());
    }
    conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
    try (OutputStream outputStream = conn.getOutputStream();
        InputStream inputStream = new ByteArrayInputStream(bytes)) {
      ByteStreams.copy(inputStream, outputStream);
    }
  }

  @Override
  public void delete(URI uri) throws IOException {
    logger.debug("Delete {}", uri);
    HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
    conn.setRequestMethod("DELETE");
    conn.getInputStream();
  }
}
