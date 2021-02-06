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
package com.baremaps.tile;

import java.util.function.Consumer;

public class Tiler implements Consumer<Tile> {

  public final TileStore tileSource;

  public final TileStore tileTarget;

  public final boolean deleteEmptyTiles;

  public Tiler(TileStore tileSource, TileStore tileTarget) {
    this(tileSource, tileTarget, false);
  }

  public Tiler(TileStore tileSource, TileStore tileTarget, boolean deleteEmptyTiles) {
    this.tileSource = tileSource;
    this.tileTarget = tileTarget;
    this.deleteEmptyTiles = deleteEmptyTiles;
  }

  @Override
  public void accept(Tile tile) {
    try {
      byte[] bytes = tileSource.read(tile);
      if (bytes != null) {
        tileTarget.write(tile, bytes);
      } else if (deleteEmptyTiles) {
        tileTarget.delete(tile);
      }
    } catch (TileStoreException ex) {
      throw new RuntimeException("An error occurred while creating the tiles", ex);
    }
  }

}
