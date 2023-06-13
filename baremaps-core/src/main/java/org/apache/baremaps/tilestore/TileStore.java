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

package org.apache.baremaps.tilestore;



import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/** Represents a store for tiles. */
public interface TileStore {

  /**
   * Gets the content of a tile.
   *
   * @param tileCoord the tile coordinate
   * @return the content of the tile
   * @throws TileStoreException
   */
  ByteBuffer get(TileCoord tileCoord) throws TileStoreException;

  /**
   * Gets the content of several tiles.
   *
   * @param tileCoords the tile coordinates
   * @return the content of the tiles
   * @throws TileStoreException
   */
  default List<ByteBuffer> get(List<TileCoord> tileCoords) throws TileStoreException {
    var blobs = new ArrayList<ByteBuffer>(tileCoords.size());
    for (var tileCoord : tileCoords) {
      blobs.add(get(tileCoord));
    }
    return blobs;
  }

  /**
   * Puts the content of a tile.
   *
   * @param tileCoord the tile coordinate
   * @param blob the content of the tile
   * @throws TileStoreException
   */
  void put(TileCoord tileCoord, ByteBuffer blob) throws TileStoreException;

  /**
   * Puts the content of several tiles.
   *
   * @param tileCoords the tile coordinates
   * @param blobs the content of the tiles
   * @throws TileStoreException
   */
  default void put(List<TileCoord> tileCoords, List<ByteBuffer> blobs) throws TileStoreException {
    for (int i = 0; i < tileCoords.size(); i++) {
      put(tileCoords.get(i), blobs.get(i));
    }
  }

  /**
   * Deletes the content of a tile.
   *
   * @param tileCoord the tile coordinate
   * @throws TileStoreException
   */
  void delete(TileCoord tileCoord) throws TileStoreException;
}
