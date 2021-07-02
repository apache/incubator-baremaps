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

package com.baremaps.blob.s3;

import com.baremaps.blob.BlobStore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3BlobStore implements BlobStore {

  private final S3Client client;

  public S3BlobStore() {
    this(S3Client.create());
  }

  public S3BlobStore(S3Client client) {
    this.client = client;
  }

  @Override
  public long size(URI uri) {
    HeadObjectRequest request = HeadObjectRequest.builder()
        .bucket(uri.getHost())
        .key(uri.getPath().substring(1))
        .build();
    return client.headObject(request).contentLength();
  }

  @Override
  public InputStream read(URI uri) throws IOException {
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
    return write(uri, Map.of());
  }

  @Override
  public OutputStream write(URI uri, Map<String, String> metadata) throws IOException {
    return new ByteArrayOutputStream() {
      @Override
      public void close() throws IOException {
        try {
          byte[] bytes = this.toByteArray();
          PutObjectRequest request = PutObjectRequest.builder()
              .bucket(uri.getHost())
              .key(uri.getPath().substring(1))
              .metadata(metadata)
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
    writeByteArray(uri, bytes, Map.of());
  }

  @Override
  public void writeByteArray(URI uri, byte[] bytes, Map<String, String> metadata) throws IOException {
    try {
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(uri.getHost())
          .key(uri.getPath().substring(1))
          .metadata(metadata)
          .build();
      client.putObject(request, RequestBody.fromBytes(bytes));
    } catch (S3Exception ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public void delete(URI uri) throws IOException {
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

}
