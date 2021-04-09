package com.baremaps.config;

import com.baremaps.blob.BlobStore;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * A base class for reading and writing configuration files in a BlobStore.
 */
public class BlobMapper {

  private final BlobStore blobStore;

  public BlobMapper(BlobStore blobStore) {
    this.blobStore = blobStore;
  }

  public boolean exists(URI uri) {
    try {
      blobStore.read(uri);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public <T> T read(URI uri, Class<T> mainType) throws IOException {
    byte[] bytes = blobStore.readByteArray(uri);
    return objectMapper(uri).readValue(bytes, mainType);
  }

  public void write(URI uri, Object object) throws IOException {
    DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
    pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    byte[] bytes = objectMapper(uri).writer(pp).writeValueAsBytes(object);
    blobStore.writeByteArray(uri, bytes);
  }

  private ObjectMapper objectMapper(URI uri) {
    JsonFactory factory = uri.toString().endsWith(".json") ? new JsonFactory() : new YAMLFactory();
    ObjectMapper mapper = new ObjectMapper(factory);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return mapper;
  }

}
