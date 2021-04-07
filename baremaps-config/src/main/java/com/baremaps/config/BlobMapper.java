package com.baremaps.config;

import static com.baremaps.config.Variables.interpolate;

import com.baremaps.blob.BlobStore;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
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
    JsonFactory jsonFactory = uri.getPath().endsWith(".json") ? new JsonFactory() : new YamlConfigFactory();
    ObjectMapper mapper = new ObjectMapper(jsonFactory);
    SimpleModule module = new SimpleModule();
    mapper.registerModules(module);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return mapper.readValue(blobStore.readByteArray(uri), mainType);
  }

  public void write(URI uri, Object object) throws IOException {
    JsonFactory jsonFactory = uri.getPath().endsWith(".json") ? new JsonFactory() : new YamlConfigFactory();
    ObjectMapper mapper = new ObjectMapper(jsonFactory);
    SimpleModule module = new SimpleModule();
    mapper.registerModules(module);
    DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
    pp.indentArraysWith( DefaultIndenter.SYSTEM_LINEFEED_INSTANCE );
    blobStore.writeByteArray(uri, mapper.writer(pp).writeValueAsBytes(object));
  }

  private class YamlConfigFactory extends YAMLFactory {

    public YamlConfigFactory() {
      super();
      disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
    }

    @Override
    protected YAMLParser _createParser(InputStream in, IOContext ctxt) throws IOException {
      return new YAMLConfigParser(ctxt, _getBufferRecycler(), _parserFeatures, _yamlParserFeatures,
          _objectCodec, _createReader(in, null, ctxt));
    }

    @Override
    protected YAMLParser _createParser(Reader r, IOContext ctxt) {
      return new YAMLConfigParser(ctxt, _getBufferRecycler(), _parserFeatures, _yamlParserFeatures,
          _objectCodec, r);
    }

    @Override
    protected YAMLParser _createParser(char[] data, int offset, int len, IOContext ctxt, boolean recyclable) {
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
        return interpolate(variables, value);
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
        return interpolate(variables, value);
      }
      return null;
    }
  }
}
