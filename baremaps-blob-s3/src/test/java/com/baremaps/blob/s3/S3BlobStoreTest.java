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

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.baremaps.blob.BlobStore;
import com.baremaps.blob.BlobStoreTest;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

class S3BlobStoreTest extends BlobStoreTest {

  @RegisterExtension
  static final S3MockExtension S3_MOCK =
      S3MockExtension.builder().silent().withSecureConnection(false).build();

  private final S3Client s3Client = S3_MOCK.createS3ClientV2();

  @BeforeEach
  void initAll() {
    s3Client.createBucket(CreateBucketRequest.builder().bucket("test").build());
  }

  @Override
  public String createTestURI() throws IOException {
    return "s3://test/test/test.txt";
  }

  @Override
  public BlobStore createFileSystem() {
    return new S3BlobStore(s3Client);
  }
}
