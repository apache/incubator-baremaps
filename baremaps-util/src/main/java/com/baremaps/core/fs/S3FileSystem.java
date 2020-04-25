/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.core.fs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3FileSystem extends FileSystem {

  private final String contentEncoding;

  private final String contentType;

  private final S3Client client;

  public S3FileSystem() {
    this("utf-8", "application/octet-stream",
        S3Client.builder().region(Region.US_WEST_2).build());
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
    GetObjectRequest request = GetObjectRequest.builder()
        .bucket(uri.getHost())
        .key(uri.getPath())
        .build();
    return client.getObject(request, ResponseTransformer.toInputStream());
  }

  @Override
  public OutputStream write(URI uri) throws IOException {
    return new ByteArrayOutputStream() {
      @Override
      public void close() throws IOException {
        byte[] bytes = this.toByteArray();
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(uri.getHost())
            .key(uri.getPath())
            .contentEncoding(contentEncoding)
            .contentType(contentType)
            .contentLength(Long.valueOf(bytes.length))
            .build();
        client.putObject(request, RequestBody.fromBytes(bytes));
      }
    };
  }

  @Override
  public void delete(URI uri) throws IOException {
    DeleteObjectRequest request = DeleteObjectRequest.builder()
        .bucket(uri.getHost())
        .key(uri.getPath())
        .build();
    client.deleteObject(request);
  }

}
