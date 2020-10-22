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

package com.baremaps.util.storage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

class LocalBlobStoreTest extends BlobStoreTest {

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
        "https://www.test.com/test.txt",
        "s3://test/test/test.txt");
  }

  @Override
  protected List<String> createValidURIList() {
    return Arrays.asList(
        "test.txt",
        "/test.txt",
        "test/test.txt",
        "/test/test.txt");
  }

  @Override
  protected BlobStore createFileSystem() {
    return new LocalBlobStore();
  }

}