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

package org.apache.baremaps.database.tile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public abstract class TileStoreTest {

  // TODO: try to move this in the testing module

  @Test
  @Tag("integration")
  void readWriteDeleteTile() throws Exception {
    TileStore tileStore = createTileStore();
    Tile tile = new Tile(1, 2, 3);
    ByteBuffer blob = ByteBuffer.wrap("tile_content".getBytes());

    // Write data
    tileStore.write(tile, blob);

    // Read the data
    ByteBuffer inputStream = tileStore.read(tile);
    assertArrayEquals(blob.array(), inputStream.array());

    // Delete the data
    tileStore.delete(tile);
    assertThrows(TileStoreException.class, () -> tileStore.read(tile));
  }

  abstract TileStore createTileStore() throws Exception;
}
