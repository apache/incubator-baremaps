/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.tilestore;



import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
   * Reads the content of a list of tiles.
   *
   * @param tileCoords the tile coordinates
   * @return the content of the tiles
   * @throws TileStoreException
   */
  default List<ByteBuffer> read(List<TileCoord> tileCoords) throws TileStoreException {
    var blobs = new ArrayList<ByteBuffer>();
    for (var tileCoord : tileCoords) {
      blobs.add(read(tileCoord));
    }
    return blobs;
  }

  /**
   * Writes the content of a tile.
   *
   * @param tileCoord the tile coordinate
   * @param blob the content of the tile
   * @throws TileStoreException
   */
  void write(TileCoord tileCoord, ByteBuffer blob) throws TileStoreException;

  /**
   * Writes the content of a list of tiles.
   *
   * @param entries the tile entries
   * @throws TileStoreException
   */
  default void write(List<TileEntry> entries) throws TileStoreException {
    for (var entry : entries) {
      write(entry.getTileCoord(), entry.getByteBuffer());
    }
  }

  /**
   * Deletes the content of a tile.
   *
   * @param tileCoord the tile coordinate
   * @throws TileStoreException
   */
  void delete(TileCoord tileCoord) throws TileStoreException;

  /**
   * Deletes the content of a list of tiles.
   *
   * @param tileCoords the tile coordinates
   * @throws TileStoreException
   */
  default void delete(List<TileCoord> tileCoords) throws TileStoreException {
    for (var tileCoord : tileCoords) {
      delete(tileCoord);
    }
  }
}
