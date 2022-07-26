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

package com.baremaps.server.utils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

public class ConfigReader {

  static {
    System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
  }

  private final Context context =
      Context.newBuilder("js")
          .option("js.esm-eval-returns-exports", "true")
          .allowExperimentalOptions(true)
          .allowIO(true)
          .build();

  public ConfigReader() {}

  public String read(Path path) throws IOException {
    var extension = com.google.common.io.Files.getFileExtension(path.toString());
    var config =
        switch (extension) {
          case "js" -> eval(path);
          case "json" -> Files.readString(path);
          default -> throw new UnsupportedOperationException("Unsupported config format");
        };
    return config;
  }

  private String eval(Path path) throws IOException {
    try {
      var script =
          String.format(
              """
          import json from '%s';
          export default JSON.stringify(json);
          """,
              path.toAbsolutePath());
      var source =
          Source.newBuilder("js", new StringReader(script), "script.js")
              .mimeType("application/javascript+module")
              .build();
      var value = context.eval(source);
      var config = value.getMember("default").toString();
      return config;
    } catch (Exception e) {
      throw new IOException(e);
    }
  }
}
