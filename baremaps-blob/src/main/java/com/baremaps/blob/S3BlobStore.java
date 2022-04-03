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
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * A {@code BlobStore} for reading and writing blobs in Amazon S3. It assumes that the host has been
 * configured and has access to the targeted S3 bucket.
 */
public class S3BlobStore implements BlobStore {

  private final S3Client client;

  /** Constructs an {@code S3BlobStore} with the default {@code S3Client}. */
  public S3BlobStore() {
    this(S3Client.create());
  }

  /**
   * Constructs an {@code S3BlobStore} with the specified {@code S3Client}.
   *
   * @param client the S3 client
   */
  public S3BlobStore(S3Client client) {
    this.client = client;
  }

  /** {@inheritDoc} */
  @Override
  public Blob head(URI uri) throws BlobStoreException {
    try {
      HeadObjectRequest request =
          HeadObjectRequest.builder().bucket(uri.getHost()).key(uri.getPath().substring(1)).build();
      HeadObjectResponse response = client.headObject(request);
      return Blob.builder()
          .withContentLength(response.contentLength())
          .withContentType(response.contentType())
          .withContentEncoding(response.contentEncoding())
          .build();
    } catch (S3Exception e) {
      throw new BlobStoreException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Blob get(URI uri) throws BlobStoreException {
    try {
      GetObjectRequest request =
          GetObjectRequest.builder().bucket(uri.getHost()).key(uri.getPath().substring(1)).build();
      ResponseInputStream<GetObjectResponse> responseInputStream = client.getObject(request);
      GetObjectResponse getObjectResponse = responseInputStream.response();
      return Blob.builder()
          .withContentLength(getObjectResponse.contentLength())
          .withContentType(getObjectResponse.contentType())
          .withContentEncoding(getObjectResponse.contentEncoding())
          .withInputStream(responseInputStream)
          .build();
    } catch (S3Exception e) {
      throw new BlobStoreException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void put(URI uri, Blob blob) throws BlobStoreException {
    try {
      PutObjectRequest.Builder builder =
          PutObjectRequest.builder()
              .bucket(uri.getHost())
              .key(uri.getPath().substring(1))
              .contentLength(blob.getContentLength())
              .contentType(blob.getContentType())
              .contentEncoding(blob.getContentEncoding());
      RequestBody requestBody =
          RequestBody.fromInputStream(blob.getInputStream(), blob.getContentLength());
      client.putObject(builder.build(), requestBody);
    } catch (S3Exception e) {
      throw new BlobStoreException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void delete(URI uri) throws BlobStoreException {
    try {
      DeleteObjectRequest request =
          DeleteObjectRequest.builder()
              .bucket(uri.getHost())
              .key(uri.getPath().substring(1))
              .build();
      client.deleteObject(request);
    } catch (S3Exception e) {
      throw new BlobStoreException(e);
    }
  }
}
