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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.net.URI;

/** A base class for reading and writing configuration files in a BlobStore. */
public class BlobMapper {

  private final BlobStore blobStore;

  public BlobMapper(BlobStore blobStore) {
    this.blobStore = blobStore;
  }

  public boolean exists(URI uri) {
    try {
      blobStore.get(uri);
      return true;
    } catch (BlobStoreException e) {
      return false;
    }
  }

  public <T> T read(URI uri, Class<T> mainType) throws BlobMapperException {
    try {
      byte[] bytes = blobStore.get(uri).getInputStream().readAllBytes();
      return objectMapper(uri).readValue(bytes, mainType);
    } catch (Exception e) {
      throw new BlobMapperException(e);
    }
  }

  public void write(URI uri, Object object) throws BlobMapperException {
    try {
      DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
      pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
      byte[] bytes = objectMapper(uri).writer(pp).writeValueAsBytes(object);
      blobStore.put(uri, Blob.builder().withByteArray(bytes).build());
    } catch (Exception e) {
      throw new BlobMapperException(e);
    }
  }

  private ObjectMapper objectMapper(URI uri) {
    JsonFactory factory = uri.toString().endsWith(".json") ? new JsonFactory() : new YAMLFactory();
    ObjectMapper mapper = new ObjectMapper(factory);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return mapper;
  }
}
