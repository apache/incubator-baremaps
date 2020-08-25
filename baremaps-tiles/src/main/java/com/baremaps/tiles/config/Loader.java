package com.baremaps.tiles.config;

import com.baremaps.util.storage.BlobStore;
import com.baremaps.util.storage.LocalBlobStore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Loader {

  private final BlobStore blobStore;

  public Loader(BlobStore blobStore) {
    this.blobStore = blobStore;
  }

  private ObjectMapper mapper() {
    return new ObjectMapper(new YAMLFactory());
  }

  private ObjectMapper loadingMapper(URI uri) {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(Layer.class, new LoadingDeserializer<>(Layer.class, uri));
    module.addDeserializer(Component.class, new LoadingDeserializer<>(Component.class, uri));
    return mapper().registerModules(module);
  }

  public Config load(URI uri) throws IOException {
    return loadingMapper(uri).readValue(blobStore.readByteArray(uri), Config.class);
  }

  private class LoadingDeserializer<T> extends StdDeserializer<T> {

    private Class<T> type;

    private final URI uri;

    public LoadingDeserializer(Class<T> type, URI uri) {
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
        return mapper().readValue(node.traverse(), type);
      }
    }
  }

  public static void main(String[] args) throws URISyntaxException, IOException {
    Loader loader = new Loader(new LocalBlobStore());
    Config config = loader.load(new URI("/Users/bchapuis/Projects/baremaps/openstreetmap-vecto/config/test.yaml"));
    System.out.println(config);
  }

}