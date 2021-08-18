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

package com.baremaps.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.blob.ResourceBlobStore;
import com.baremaps.config.style.Style;
import com.baremaps.config.tileset.Tileset;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class BlobMapperTest {

  @Test
  void loadJsonTileset() throws URISyntaxException, BlobMapperException {
    Tileset tileset =
        new BlobMapper(new ResourceBlobStore())
            .read(new URI("res://./tileset.json"), Tileset.class);
    assertEquals(1, tileset.getVectorLayers().size());
    assertEquals("layer", tileset.getVectorLayers().get(0).getId());
  }

  @Test
  void loadJsonStyle() throws URISyntaxException, BlobMapperException {
    Style style =
        new BlobMapper(new ResourceBlobStore()).read(new URI("res://./style.json"), Style.class);
    assertEquals("style", style.getName());
  }

  @Test
  void loadYamlStyle() throws URISyntaxException, BlobMapperException {
    Style style =
        new BlobMapper(new ResourceBlobStore()).read(new URI("res://./style.yaml"), Style.class);
    assertEquals("style", style.getName());
  }
}
