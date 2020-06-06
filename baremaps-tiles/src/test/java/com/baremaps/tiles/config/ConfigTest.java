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

import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.postgresql.util.ReaderInputStream;

class ConfigTest {

  @Test
  public void load() throws IOException {
    InputStream input = this.getClass().getClassLoader().getResourceAsStream("config.yaml");
    Config config = Config.load(input);
    assertNotNull(config);
    assertTrue(config.getLayers().size() == 2);
  }

  @Test
  public void loadEmptyYamlConfig() throws IOException {
    InputStream input = new ReaderInputStream(CharSource.wrap("").openStream());
    Config config = Config.load(input);
    assertNotNull(config);
    assertEquals(config.getId(), "baremaps");
  }

  @Test
  public void loadYamlConfig() throws IOException {
    InputStream input = new ReaderInputStream(CharSource.wrap("id: test").openStream());
    Config config = Config.load(input);
    assertNotNull(config);
    assertEquals(config.getId(), "test");
  }

  @Test
  public void checkOverrideBehavior() throws IOException {
    InputStream input = new ReaderInputStream(CharSource.wrap("{id: test, center: {lon: 10}}").openStream());
    Config config = Config.load(input);
    assertNotNull(config);
    assertEquals(config.getId(), "test");
    assertEquals(config.getCenter().getLon(), 10);
    assertEquals(config.getCenter().getLat(), 0);
  }

}