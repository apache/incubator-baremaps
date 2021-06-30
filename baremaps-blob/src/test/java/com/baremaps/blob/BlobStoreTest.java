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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public abstract class BlobStoreTest {

  @Test
  @Tag("integration")
  void readWriteDelete() throws IOException, URISyntaxException {
    BlobStore blobStore = createFileSystem();
    URI uri = new URI(createTestURI());
    String content = "content";

    // Write data
    try (OutputStream outputStream = blobStore.write(uri)) {
      outputStream.write(content.getBytes(Charsets.UTF_8));
    }

    // Read the data
    try (InputStream inputStream = blobStore.read(uri)) {
      assertEquals(content, CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8)));
    }

    // Delete the data
    blobStore.delete(uri);
    assertThrows(IOException.class, () -> {
      blobStore.read(uri).close();
    });
  }

  protected abstract String createTestURI() throws IOException;

  protected abstract BlobStore createFileSystem();

}