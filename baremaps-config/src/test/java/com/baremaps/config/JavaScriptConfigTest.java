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

package com.baremaps.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baremaps.blob.Blob;
import com.baremaps.blob.BlobStoreException;
import com.baremaps.blob.FileBlobStore;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class JavaScriptConfigTest {

  @Test
  void read() throws IOException, ConfigException, BlobStoreException {
    var uri = URI.create("file://./tmp/test.txt");
    var store = new FileBlobStore();
    store.put(uri, Blob.builder().withByteArray("export default 'test'".getBytes(StandardCharsets.UTF_8)).build());
    var config = new JavaScriptConfig(store, uri);
    var content = config.read();
    assertArrayEquals("test".getBytes(StandardCharsets.UTF_8), content);
  }

  @Test
  void write() throws IOException {
    var uri = URI.create("file://./tmp/test.txt");
    var store = new FileBlobStore();
    var config = new JavaScriptConfig(store, uri);
    var content = "test".getBytes(StandardCharsets.UTF_8);
    assertThrows(
        UnsupportedOperationException.class,
        () -> config.write(content));
  }
}
