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

package com.baremaps.util.fs;

import com.google.common.base.Charsets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3FileSystem extends FileSystem {

  private static Logger logger = LogManager.getLogger();

  private final String contentEncoding;

  private final String contentType;

  private final S3Client client;

  public S3FileSystem() {
    this("utf-8", "application/octet-stream", S3Client.create());
  }

  public S3FileSystem(String contentEncoding, String contentType, S3Client client) {
    this.contentEncoding = contentEncoding;
    this.contentType = contentType;
    this.client = client;
  }

  @Override
  public boolean accept(URI uri) {
    return "s3".equals(uri.getScheme())
        && uri.getHost() != null
        && uri.getPath() != null;
  }

  @Override
  public InputStream read(URI uri) throws IOException {
    logger.debug("Read {}", uri);
    try {
      GetObjectRequest request = GetObjectRequest.builder()
          .bucket(uri.getHost())
          .key(uri.getPath().substring(1))
          .build();
      return client.getObject(request, ResponseTransformer.toInputStream());
    } catch (S3Exception ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public byte[] readByteArray(URI uri) throws IOException {
    logger.debug("Write {}", uri);
    try {
      GetObjectRequest request = GetObjectRequest.builder()
          .bucket(uri.getHost())
          .key(uri.getPath().substring(1))
          .build();
      return client.getObject(request, ResponseTransformer.toBytes()).asByteArray();
    } catch (S3Exception ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public OutputStream write(URI uri) throws IOException {
    logger.debug("Write {}", uri);
    return new ByteArrayOutputStream() {
      @Override
      public void close() throws IOException {
        try {
          byte[] bytes = this.toByteArray();
          PutObjectRequest request = PutObjectRequest.builder()
              .bucket(uri.getHost())
              .key(uri.getPath().substring(1))
              .build();
          client.putObject(request, RequestBody.fromBytes(bytes));
        } catch (S3Exception ex) {
          throw new IOException(ex);
        }
      }
    };
  }

  @Override
  public void writeByteArray(URI uri, byte[] bytes) throws IOException {
    logger.debug("Write {}", uri);
    try {
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(uri.getHost())
          .key(uri.getPath().substring(1))
          .build();
      client.putObject(request, RequestBody.fromBytes(bytes));
    } catch (S3Exception ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public void delete(URI uri) throws IOException {
    logger.debug("Delete {}", uri);
    try {
      DeleteObjectRequest request = DeleteObjectRequest.builder()
          .bucket(uri.getHost())
          .key(uri.getPath().substring(1))
          .build();
      client.deleteObject(request);
    } catch (S3Exception ex) {
      throw new IOException(ex);
    }
  }

  public static void main(String[] args) throws URISyntaxException, IOException {
    S3FileSystem fs = new S3FileSystem();
    fs.writeByteArray(new URI("s3://baremaps-test/tiles/"), "test".getBytes(Charsets.UTF_8));
  }

}
