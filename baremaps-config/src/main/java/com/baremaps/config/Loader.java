package com.baremaps.config;

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

class Loader<T> {

  private final BlobStore blobStore;

  private final Class<T> mainType;

  private final Class<?>[] externalTypes;

  public Loader(BlobStore blobStore, Class<T> mainType, Class<?>... externalTypes) {
    this.blobStore = blobStore;
    this.mainType = mainType;
    this.externalTypes = externalTypes;
  }

  public T load(URI uri) throws IOException {
    YAMLFactory yamlFactory = new YAMLFactory();
    SimpleModule module = new SimpleModule();
    for (Class<?> externalType : externalTypes) {
      module.addDeserializer(externalType, new StdDeserializer(externalType) {
        @Override
        public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException {
          ObjectMapper mapper = new ObjectMapper(yamlFactory);
          JsonNode node = parser.getCodec().readTree(parser);
          if (node.isTextual()) {
            URI ref = uri.resolve(node.asText());
            return mapper.readValue(blobStore.readByteArray(ref), externalType);
          } else {
            JsonParser p = node.traverse();
            return mapper.readValue(p, externalType);
          }
        }
      });
    }
    return new ObjectMapper(yamlFactory)
        .registerModules(module)
        .readValue(blobStore.readByteArray(uri), mainType);
  }

}
