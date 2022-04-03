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

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ResourceBlobStoreTest {

  @Test
  @Tag("integration")
  void test() throws IOException, BlobStoreException {
    BlobStore blobStore = new ResourceBlobStore();

    // Read the data
    try (InputStream inputStream = blobStore.get(URI.create("res:///blob.txt")).getInputStream()) {
      assertEquals(
          "test", CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8)));
    }

    // Get unexisting blob
    try (InputStream ignored =
        blobStore.get(URI.create("res:///missing-blob.txt")).getInputStream()) {
      fail("Expected an IOException to be thrown");
    } catch (BlobStoreException e) {
      // Test exception message...
    }
  }
}
