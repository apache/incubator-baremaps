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

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class ConfigTest {

  @Test
  public void loadEmptyYamlConfig() {
    Yaml yaml = new Yaml(new ConfigConstructor());
    Config config = yaml.load("");
    assertNotNull(config);
    assertEquals(config.getId(), "baremaps");
  }

  @Test
  public void loadYamlConfig() {
    Yaml yaml = new Yaml(new ConfigConstructor());
    Config config = yaml.load("id: test");
    assertNotNull(config);
    assertEquals(config.getId(), "test");
  }

  @Test
  public void checkOverrideBehavior() {
    Yaml yaml = new Yaml(new ConfigConstructor());
    Config config = yaml.load("{id: test, center: {lon: 10}}");
    assertNotNull(config);
    assertEquals(config.getId(), "test");
    assertEquals(config.getCenter().getLon(), 10);
    assertEquals(config.getCenter().getLat(), 0);
  }

}