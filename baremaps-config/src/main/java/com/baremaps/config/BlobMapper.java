package com.baremaps.config;

import com.baremaps.blob.BlobStore;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * A base class for reading and writing configuration files in a BlobStore.
 */
public class BlobMapper {

  private final BlobStore blobStore;

  private final Map<String, String> variables;

  public BlobMapper(BlobStore blobStore) {
    this(blobStore, System.getenv());
  }

  public BlobMapper(BlobStore blobStore, Map<String, String> variables) {
    this.blobStore = blobStore;
    this.variables = variables;
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
    ObjectMapper mapper = new ObjectMapper(new JsonFactory());
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return mapper.readValue(blobStore.readByteArray(uri), mainType);
  }

  public void write(URI uri, Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper(new JsonFactory());
    DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
    pp.indentArraysWith( DefaultIndenter.SYSTEM_LINEFEED_INSTANCE );
    blobStore.writeByteArray(uri, mapper.writer(pp).writeValueAsBytes(object));
  }

}
