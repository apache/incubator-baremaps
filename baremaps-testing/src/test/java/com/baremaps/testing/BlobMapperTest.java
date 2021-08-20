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

package com.baremaps.testing;

import com.baremaps.blob.BlobMapper;
import com.baremaps.blob.BlobMapperException;
import com.baremaps.blob.ResourceBlobStore;
import com.baremaps.model.MbStyle;
import com.baremaps.model.TileSet;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BlobMapperTest {

  @Test
  void loadJsonTileset() throws URISyntaxException, BlobMapperException {
    TileSet tileset =
        new BlobMapper(new ResourceBlobStore())
            .read(new URI("res://./tileset.json"), TileSet.class);
    Assertions.assertEquals(1, tileset.getVectorLayers().size());
    Assertions.assertEquals("layer", tileset.getVectorLayers().get(0).getId());
  }

  @Test
  void loadJsonStyle() throws URISyntaxException, BlobMapperException {
    MbStyle style =
        new BlobMapper(new ResourceBlobStore()).read(new URI("res://./style.json"), MbStyle.class);
    Assertions.assertEquals("style", style.getName());
  }

  @Test
  void loadYamlStyle() throws URISyntaxException, BlobMapperException {
    MbStyle style =
        new BlobMapper(new ResourceBlobStore()).read(new URI("res://./style.yaml"), MbStyle.class);
    Assertions.assertEquals("style", style.getName());
  }
}
