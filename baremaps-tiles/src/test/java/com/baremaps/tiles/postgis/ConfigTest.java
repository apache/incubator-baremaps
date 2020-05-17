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

package com.baremaps.tiles.postgis;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baremaps.tiles.config.Config;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

public class ConfigTest {

  @Test
  public void load() {
    InputStream input = this.getClass().getClassLoader().getResourceAsStream("config.yaml");
    Config config = Config.load(input);
    assertNotNull(config);
    assertTrue(config.getLayers().size() == 2);
  }

}
