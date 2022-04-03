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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 * A {@link BlobStore} used exclusively for reading and writing configuration files. Different file
 * formats are supported, some of which are interpretted. For instance, Json files can be read,
 * written and are always pretty printed. Javascript files are interpreted with graaljs and can only
 * be read. Operations executed on other formats throw {@link UnsupportedOperationException}.
 */
public class ConfigBlobStore implements BlobStore {

  static {
    System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
  }

  private final BlobStore blobStore;

  private final ObjectMapper mapper = new ObjectMapper();

  private final Context context =
      Context.newBuilder("js")
          .option("js.esm-eval-returns-exports", "true")
          .allowExperimentalOptions(true)
          .allowIO(true)
          .build();

  public ConfigBlobStore(BlobStore blobStore) {
    this.blobStore = blobStore;
  }

  @Override
  public Blob head(URI uri) throws BlobStoreException {
    String extension = Files.getFileExtension(uri.getPath());
    switch (extension) {
      case "js":
      case "json":
        return blobStore.head(uri);
      default:
        throw new UnsupportedOperationException("Unsupported config format");
    }
  }

  @Override
  public Blob get(URI uri) throws BlobStoreException {
    String extension = Files.getFileExtension(uri.getPath());
    switch (extension) {
      case "js":
        return eval(uri);
      case "json":
        return blobStore.get(uri);
      default:
        throw new UnsupportedOperationException("Unsupported config format");
    }
  }

  private Blob eval(URI uri) throws BlobStoreException {
    try {
      var blob = blobStore.get(uri);
      try (Reader reader = new InputStreamReader(blob.getInputStream())) {
        Source source =
            Source.newBuilder("js", reader, "config.js")
                .mimeType("application/javascript+module")
                .build();
        Value value = context.eval(source);
        return Blob.builder()
            .withByteArray(value.getMember("default").toString().getBytes(StandardCharsets.UTF_8))
            .build();
      }
    } catch (Exception e) {
      throw new BlobStoreException(e);
    }
  }

  @Override
  public void put(URI uri, Blob blob) throws BlobStoreException {
    String extension = Files.getFileExtension(uri.getPath());
    switch (extension) {
      case "json":
        blobStore.put(uri, blob);
        return;
      default:
        throw new UnsupportedOperationException("Unsupported config format");
    }
  }

  @Override
  public void delete(URI uri) throws BlobStoreException {
    String extension = Files.getFileExtension(uri.getPath());
    switch (extension) {
      case "json":
        blobStore.delete(uri);
        return;
      default:
        throw new UnsupportedOperationException("Unsupported config format");
    }
  }
}
