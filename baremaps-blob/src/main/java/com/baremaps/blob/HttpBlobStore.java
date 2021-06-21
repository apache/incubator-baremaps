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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpBlobStore implements BlobStore {

  private static Logger logger = LoggerFactory.getLogger(HttpBlobStore.class);

  private final String contentEncoding;

  private final String contentType;

  public HttpBlobStore() {
    this("utf-8", "application/octet-stream");
  }

  public HttpBlobStore(String contentEncoding, String contentType) {
    this.contentEncoding = contentEncoding;
    this.contentType = contentType;
  }

  @Override
  public InputStream read(URI uri) throws IOException {
    logger.info("Read {}", uri);
    HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("GET");
    return new BufferedInputStream(conn.getInputStream());
  }

  @Override
  public byte[] readByteArray(URI uri) throws IOException {
    logger.info("Read {}", uri);
    HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("GET");
    try (InputStream inputStream = conn.getInputStream()) {
      return ByteStreams.toByteArray(inputStream);
    }
  }

  @Override
  public OutputStream write(URI uri) {
    logger.info("Write {}", uri);
    return new ByteArrayOutputStream() {
      @Override
      public void close() throws IOException {
        byte[] bytes = this.toByteArray();
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestProperty("Content-Encoding", contentEncoding);
        conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
        try (OutputStream outputStream = conn.getOutputStream();
            InputStream inputStream = new ByteArrayInputStream(bytes)) {
          ByteStreams.copy(inputStream, outputStream);
        }
      }
    };
  }

  @Override
  public void writeByteArray(URI uri, byte[] bytes) throws IOException {
    logger.info("Write {}", uri);
    HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("PUT");
    conn.setRequestProperty("Content-Type", contentType);
    conn.setRequestProperty("Content-Encoding", contentEncoding);
    conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
    try (OutputStream outputStream = conn.getOutputStream();
        InputStream inputStream = new ByteArrayInputStream(bytes)) {
      ByteStreams.copy(inputStream, outputStream);
    }
  }

  @Override
  public void delete(URI uri) throws IOException {
    logger.info("Delete {}", uri);
    HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("DELETE");
    conn.getInputStream();
  }
}
