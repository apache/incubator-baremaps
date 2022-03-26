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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ConfigStoreTest {

  static final URI JSON = URI.create("file://./tmp/test.json");
  static final URI JS = URI.create("file://./tmp/test.js");

  static final byte[] RESULT = "test".getBytes(StandardCharsets.UTF_8);
  static final byte[] SCRIPT = "export default 'test'".getBytes(StandardCharsets.UTF_8);

  @Test
  void json() throws BlobStoreException, IOException {
    var fileStore = new FileBlobStore();
    fileStore.put(JSON, Blob.builder().withByteArray(RESULT).build());
    var configStore = new ConfigBlobStore(fileStore);
    assertArrayEquals(RESULT, configStore.get(JSON).getInputStream().readAllBytes());
  }

  @Test
  void javascript() throws BlobStoreException, IOException {
    var fileStore = new FileBlobStore();
    fileStore.put(JS, Blob.builder().withByteArray(SCRIPT).build());
    var configStore = new ConfigBlobStore(fileStore);
    assertArrayEquals(RESULT, configStore.get(JSON).getInputStream().readAllBytes());
  }
}
