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

package com.baremaps.tiles.store;

import com.baremaps.util.storage.LocalBlobStore;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

class BlobStoreTileStoreTest extends TileStoreTest {

  @Override
  protected TileStore createTileStore() throws IOException, URISyntaxException {
    Path directory = Files.createTempDirectory("baremaps_");
    return new BlobTileStore(new LocalBlobStore(), new URI(directory.toString()));
  }

}