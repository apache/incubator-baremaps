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

package com.baremaps.core.config;

import com.baremaps.core.blob.Blob;
import com.baremaps.core.blob.BlobStore;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 * A read only {@link Config} computed by evaluating a JavaScript file. The configuration
 * corresponds to the default export of ECMAScript module.
 */
public class JavaScriptConfig implements Config {

  static {
    System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
  }

  private final BlobStore store;

  private final URI uri;

  private final Context context =
      Context.newBuilder("js")
          .option("js.esm-eval-returns-exports", "true")
          .allowExperimentalOptions(true)
          .allowIO(true)
          .build();

  /**
   * Constructs a javascript configuration.
   *
   * @param store the blob store on which the configuration is stored
   * @param uri the uri of the configuration file
   */
  public JavaScriptConfig(BlobStore store, URI uri) {
    this.store = store;
    this.uri = uri;
  }

  /** {@inheritDoc} * */
  @Override
  public byte[] read() throws ConfigException {
    try {
      Blob blob = store.get(uri);
      try (Reader reader = new InputStreamReader(blob.getInputStream())) {
        Source source =
            Source.newBuilder("js", reader, "config.js")
                .mimeType("application/javascript+module")
                .build();
        Value value = context.eval(source);
        return value.getMember("default").toString().getBytes(StandardCharsets.UTF_8);
      }
    } catch (Exception e) {
      throw new ConfigException(e);
    }
  }

  /** This operation is not supported. * */
  @Override
  public void write(byte[] value) {
    throw new UnsupportedOperationException();
  }
}
