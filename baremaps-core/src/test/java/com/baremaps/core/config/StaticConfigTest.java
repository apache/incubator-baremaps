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

import static org.junit.jupiter.api.Assertions.*;

import com.baremaps.core.blob.FileBlobStore;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class StaticConfigTest {

  @Test
  void readWrite() throws IOException, ConfigException {
    var uri = URI.create("file://./tmp/test.txt");
    var store = new FileBlobStore();
    var config = new StaticConfig(store, uri);
    var content = "test".getBytes(StandardCharsets.UTF_8);
    config.write(content);
    var storedContent = config.read();
    assertArrayEquals(content, storedContent);
  }
}
