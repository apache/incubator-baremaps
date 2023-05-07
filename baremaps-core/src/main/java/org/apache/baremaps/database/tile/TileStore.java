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



import java.nio.ByteBuffer;

/** Represents a store for tiles. */
public interface TileStore {

  /**
   * Reads the content of a tile.
   *
   * @param tileCoord the tile coordinate
   * @return the content of the tile
   * @throws TileStoreException
   */
  ByteBuffer read(TileCoord tileCoord) throws TileStoreException;

  /**
   * Writes the content of a tile.
   *
   * @param tileCoord the tile coordinate
   * @param blob the content of the tile
   * @throws TileStoreException
   */
  void write(TileCoord tileCoord, ByteBuffer blob) throws TileStoreException;

  /**
   * Deletes the content of a tile.
   *
   * @param tileCoord the tile coordinate
   * @throws TileStoreException
   */
  void delete(TileCoord tileCoord) throws TileStoreException;
}
