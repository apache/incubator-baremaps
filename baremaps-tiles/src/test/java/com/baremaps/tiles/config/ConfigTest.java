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
package com.baremaps.tiles.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ConfigTest {

  @Test
  public void load() throws IOException, URISyntaxException {
    URL url = this.getClass().getClassLoader().getResource("config.yaml");
    byte[] input =  Files.readAllBytes(Paths.get(url.toURI()));
    Config config = Config.load(input);
    assertNotNull(config);
    assertTrue(config.getLayers().size() == 2);
  }

  @Test
  public void loadEmptyYamlConfig() throws IOException {
    byte[] input = "".getBytes();
    Config config = Config.load(input);
    assertNotNull(config);
    assertEquals(config.getId(), "baremaps");
  }

  @Test
  public void loadYamlConfig() throws IOException {
    byte[] input = "id: test".getBytes();
    Config config = Config.load(input);
    assertNotNull(config);
    assertEquals(config.getId(), "test");
  }

  @Test
  public void checkOverrideBehavior() throws IOException {
    byte[] input = "{id: test, center: {lon: 10}}".getBytes();
    Config config = Config.load(input);
    assertNotNull(config);
    assertEquals(config.getId(), "test");
    assertEquals(config.getCenter().getLon(), 10);
    assertEquals(config.getCenter().getLat(), 0);
  }

}