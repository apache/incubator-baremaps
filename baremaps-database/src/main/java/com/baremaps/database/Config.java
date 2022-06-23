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

package com.baremaps.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public class Config {

  static {
    System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
  }

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final Context context =
      Context.newBuilder("js")
          .option("js.esm-eval-returns-exports", "true")
          .allowExperimentalOptions(true)
          .allowIO(true)
          .build();

  public static ByteBuffer read(Path path) throws IOException {
    String extension = com.google.common.io.Files.getFileExtension(path.toString());
    switch (extension) {
      case "js":
        return ByteBuffer.wrap(eval(path));
      case "json":
        return ByteBuffer.wrap(Files.readAllBytes(path));
      default:
        throw new UnsupportedOperationException("Unsupported config format");
    }
  }

  private static byte[] eval(Path path) throws IOException {
    try (Reader reader = Files.newBufferedReader(path)) {
      Source source =
          Source.newBuilder("js", reader, "config.js")
              .mimeType("application/javascript+module")
              .build();
      Value value = context.eval(source);
      return value.getMember("default").toString().getBytes(StandardCharsets.UTF_8);
    }
  }
}
