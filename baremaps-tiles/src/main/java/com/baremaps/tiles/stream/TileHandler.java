/*
 * Copyright (C) 2020 The baremaps Authors
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
package com.baremaps.tiles.stream;

import com.baremaps.tiles.TileStore;
import com.baremaps.util.tile.Tile;
import java.io.IOException;
import java.util.function.Consumer;

public class TileHandler implements Consumer<Tile> {

  public final TileStore tileSource;

  public final TileStore tileTarget;

  public TileHandler(TileStore tileSource, TileStore tileTarget) {
    this.tileSource = tileSource;
    this.tileTarget = tileTarget;
  }

  @Override
  public void accept(Tile tile) {
    try {
      byte[] bytes = tileSource.read(tile);
      if (bytes != null) {
        tileTarget.write(tile, bytes);
      } else {
        tileTarget.delete(tile);
      }
    } catch (IOException ex) {
      throw new RuntimeException("An error occurred while creating the tiles", ex);
    }
  }

}
