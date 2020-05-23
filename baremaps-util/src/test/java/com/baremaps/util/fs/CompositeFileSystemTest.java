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

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

public class CompositeFileSystemTest extends FileSystemTest {

  @RegisterExtension
  static final S3MockExtension S3_MOCK = S3MockExtension.builder()
      .silent()
      .withSecureConnection(false)
      .build();

  private final S3Client s3Client = S3_MOCK.createS3ClientV2();

  @BeforeEach
  void initAll() {
    s3Client.createBucket(CreateBucketRequest.builder().bucket("test").build());
  }

  @Override
  protected String createTestURI() throws IOException {
    File file = File.createTempFile("baremaps_", ".test");
    file.delete();
    return file.getPath();
  }

  @Override
  protected List<String> createWrongURIList() {
    return Arrays.asList(
        "http://www.test.com/test.txt",
        "https://www.test.com/test.txt");
  }

  @Override
  protected List<String> createValidURIList() {
    return Arrays.asList(
        "test.txt",
        "/test.txt",
        "test/test.txt",
        "/test/test.txt",
        "s3://test/test/test.txt");
  }

  @Override
  protected FileSystem createFileSystem() {
    return new CompositeFileSystem(
        new LocalFileSystem(),
        new S3FileSystem("utf-8", "application/octet-stream", s3Client));
  }

}
