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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

class S3BlobStoreTest {

  @RegisterExtension
  static final S3MockExtension S3_MOCK =
      S3MockExtension.builder().silent().withSecureConnection(false).build();

  private final S3Client s3Client = S3_MOCK.createS3ClientV2();

  @BeforeEach
  void initAll() {
    s3Client.createBucket(CreateBucketRequest.builder().bucket("test").build());
  }

  @Test
  @Tag("integration")
  void readWriteDelete() throws IOException, BlobStoreException {
    URI uri = URI.create("s3://test/test/test.txt");
    String content = "content";
    BlobStore blobStore = new S3BlobStore(s3Client);

    // Write data
    blobStore.put(uri, Blob.builder().withByteArray(content.getBytes(Charsets.UTF_8)).build());

    // Read the data
    try (InputStream inputStream = blobStore.get(uri).getInputStream()) {
      assertEquals(
          content, CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8)));
    }

    // Delete the data
    blobStore.delete(uri);

    // Get unexisting blob
    try (InputStream ignored = blobStore.get(uri).getInputStream()) {
      fail("Expected an IOException to be thrown");
    } catch (BlobStoreException e) {
      // Test exception message...
    }
  }
}
