package com.baremaps.config.legacy;

import com.baremaps.blob.BlobStore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.net.URI;

public class ConfigLoader {

  private final BlobStore blobStore;

  public ConfigLoader(BlobStore blobStore) {
    this.blobStore = blobStore;
  }

  private ObjectMapper mapper() {
    return new ObjectMapper(new YAMLFactory());
  }

  private ObjectMapper loadingMapper(URI uri) {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(Layer.class, new URIDeserializer<>(Layer.class, uri));
    module.addDeserializer(Stylesheet.class, new URIDeserializer<>(Stylesheet.class, uri));
    return mapper().registerModules(module);
  }

  public Config load(URI uri) throws IOException {
    return loadingMapper(uri).readValue(blobStore.readByteArray(uri), Config.class);
  }

  private class URIDeserializer<T> extends StdDeserializer<T> {

    private Class<T> type;

    private final URI uri;

    public URIDeserializer(Class<T> type, URI uri) {
      super(type);
      this.type = type;
      this.uri = uri;
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      JsonNode node = parser.getCodec().readTree(parser);
      if (node.isTextual()) {
        URI ref = uri.resolve(node.asText());
        return mapper().readValue(blobStore.readByteArray(ref), type);
      } else {
        JsonParser p = node.traverse();
        return mapper().readValue(p, type);
      }
    }
  }

}