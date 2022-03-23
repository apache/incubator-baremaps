/*
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

package com.baremaps.core.tile;

import com.baremaps.blob.FileBlobStore;
import com.baremaps.collection.utils.FileUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

class BlobStoreTileStoreTest extends TileStoreTest {

  Path directory;

  @BeforeEach
  void before() throws IOException {
    directory = Files.createTempDirectory(Paths.get("."), "baremaps_");
  }

  @AfterEach
  void after() throws IOException {
    FileUtils.deleteRecursively(directory);
  }

  @Override
  TileStore createTileStore() throws IOException, URISyntaxException {
    return new TileBlobStore(new FileBlobStore(), new URI(directory.toString()));
  }
}
