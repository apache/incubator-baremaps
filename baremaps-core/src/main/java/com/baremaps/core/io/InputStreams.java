/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.core.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InputStreams {

  public static InputStream from(String source) throws IOException {
    if (Files.exists(Paths.get(source))) {
      return Files.newInputStream(Paths.get(source));
    }
    try {
      return new BufferedInputStream(new URL(source).openConnection().getInputStream());
    } catch (MalformedURLException exception) {
      throw new IOException(String.format("Invalid source: %s", source));
    }
  }

}
