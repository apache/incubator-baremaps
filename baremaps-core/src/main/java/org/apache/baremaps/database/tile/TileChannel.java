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
import java.util.function.Consumer;

/** A channel that conveys tiles from a source to a target. */
public class TileChannel implements Consumer<TileCoord> {

  public final TileStore source;

  public final TileStore target;

  public final boolean deleteEmptyTiles;

  /**
   * Constructs a {@code TileChannel}.
   *
   * @param source the source
   * @param target the target
   */
  public TileChannel(TileStore source, TileStore target) {
    this(source, target, false);
  }

  /**
   * Constructs a {@code TileChannel}.
   *
   * @param source the source
   * @param target the target
   * @param deleteEmptyTiles deletes empty tiles
   */
  public TileChannel(TileStore source, TileStore target, boolean deleteEmptyTiles) {
    this.source = source;
    this.target = target;
    this.deleteEmptyTiles = deleteEmptyTiles;
  }

  /** {@inheritDoc} */
  @Override
  public void accept(TileCoord tileCoord) {
    try {
      ByteBuffer blob = source.read(tileCoord);
      if (blob != null) {
        target.write(tileCoord, blob);
      } else if (deleteEmptyTiles) {
        target.delete(tileCoord);
      }
    } catch (TileStoreException ex) {
      throw new RuntimeException("An error occurred while creating the tiles", ex);
    }
  }
}
