package com.baremaps.config;

import com.baremaps.blob.BlobStore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A Base class for loading YAML files.
 *
 * @param <T>
 */
class Loader<T> {

  private final BlobStore blobStore;

  private final Map<String, String> variables;

  private final Class<T> mainType;

  private final Class<?>[] externalTypes;

  public Loader(BlobStore blobStore, Class<T> mainType, Class<?>... externalTypes) {
    this(blobStore, System.getenv(), mainType, externalTypes);
  }

  public Loader(BlobStore blobStore, Map<String, String> variables, Class<T> mainType, Class<?>... externalTypes) {
    this.blobStore = blobStore;
    this.variables = variables;
    this.mainType = mainType;
    this.externalTypes = externalTypes;
  }

  public T load(URI uri) throws IOException {
    YAMLConfigFactory yamlFactory = new YAMLConfigFactory();
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

  private class YAMLConfigFactory extends YAMLFactory {

    @Override
    protected YAMLParser _createParser(InputStream in, IOContext ctxt) throws IOException {
      return new YAMLConfigParser(ctxt, _getBufferRecycler(), _parserFeatures, _yamlParserFeatures,
          _objectCodec, _createReader(in, null, ctxt));
    }

    @Override
    protected YAMLParser _createParser(Reader r, IOContext ctxt) throws IOException {
      return new YAMLConfigParser(ctxt, _getBufferRecycler(), _parserFeatures, _yamlParserFeatures,
          _objectCodec, r);
    }

    @Override
    protected YAMLParser _createParser(char[] data, int offset, int len, IOContext ctxt,
        boolean recyclable) throws IOException {
      return new YAMLConfigParser(ctxt, _getBufferRecycler(), _parserFeatures, _yamlParserFeatures,
          _objectCodec, new CharArrayReader(data, offset, len));
    }

    @Override
    protected YAMLParser _createParser(byte[] data, int offset, int len, IOContext ctxt) throws IOException {
      return new YAMLConfigParser(ctxt, _getBufferRecycler(), _parserFeatures, _yamlParserFeatures,
          _objectCodec, _createReader(data, offset, len, null, ctxt));
    }

  }

  private class YAMLConfigParser extends YAMLParser {

    public YAMLConfigParser(IOContext ctxt, BufferRecycler br, int parserFeatures,
        int formatFeatures, ObjectCodec codec, Reader reader) {
      super(ctxt, br, parserFeatures, formatFeatures, codec, reader);
    }

    @Override
    public String getText() throws IOException {
      final String value = super.getText();
      if (value != null) {
        return interpolateVariables(value);
      }
      return value;
    }

    @Override
    public String getValueAsString() throws IOException {
      return getValueAsString(null);
    }

    @Override
    public String getValueAsString(final String defaultValue) throws IOException {
      final String value = super.getValueAsString(defaultValue);
      if (value != null) {
        return interpolateVariables(value);
      }
      return null;
    }

    private String interpolateVariables(String value) {
      for (Entry<String, String> entry : variables.entrySet()) {
        value = value.replace(String.format("${%s}", entry.getKey()), entry.getValue());
      }
      return value;
    }

  }




}
