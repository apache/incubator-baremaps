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



import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.Weigher;
import java.nio.ByteBuffer;
import org.checkerframework.checker.index.qual.NonNegative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A {@code TileStore} decorator that uses caffeine to cache the content of tiles. */
public class TileCache implements TileStore {

  private static final Logger logger = LoggerFactory.getLogger(TileCache.class);

  private final TileStore tileStore;

  private final Cache<Tile, ByteBuffer> cache;

  /**
   * Decorates the TileStore with a cache.
   *
   * @param tileStore
   * @param spec
   */
  public TileCache(TileStore tileStore, CaffeineSpec spec) {
    this.tileStore = tileStore;
    this.cache = Caffeine.from(spec).weigher(new Weigher<Tile, ByteBuffer>() {
      @Override
      public @NonNegative int weigh(Tile tile, ByteBuffer blob) {
        return 28 + blob.capacity();
      }
    }).build();
  }

  /** {@inheritDoc} */
  @Override
  public ByteBuffer read(Tile tile) throws TileStoreException {
    return cache.get(tile, t -> {
      try {
        return tileStore.read(t).duplicate();
      } catch (TileStoreException e) {
        logger.error("Unable to read the tile.", e);
        return null;
      }
    });
  }

  /** {@inheritDoc} */
  @Override
  public void write(Tile tile, ByteBuffer bytes) throws TileStoreException {
    tileStore.write(tile, bytes);
    cache.invalidate(tile);
  }

  /** {@inheritDoc} */
  @Override
  public void delete(Tile tile) throws TileStoreException {
    tileStore.delete(tile);
    cache.invalidate(tile);
  }
}
