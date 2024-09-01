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

package org.apache.baremaps.tilestore.raster;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.Weigher;
import java.awt.image.BufferedImage;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;
import org.checkerframework.checker.index.qual.NonNegative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A {@code TileStore} decorator that uses caffeine to cache the content of tiles. */
public class RasterTileCache implements TileStore<BufferedImage> {

  private static final Logger logger = LoggerFactory.getLogger(RasterTileCache.class);

  private final TileStore<BufferedImage> tileStore;

  private final Cache<TileCoord, BufferedImage> cache;

  /**
   * Decorates the TileStore with a cache.
   *
   * @param tileStore the tile store
   * @param caffeineSpec the cache specification
   */
  public RasterTileCache(TileStore<BufferedImage> tileStore, CaffeineSpec caffeineSpec) {
    this.tileStore = tileStore;
    this.cache = Caffeine.from(caffeineSpec).weigher(new Weigher<TileCoord, BufferedImage>() {
      @Override
      public @NonNegative int weigh(TileCoord tileCoord, BufferedImage bufferedImage) {
        return bufferedImage.getData().getDataBuffer().getSize() * 4;
      }
    }).build();
  }

  /** {@inheritDoc} */
  @Override
  public BufferedImage read(TileCoord tileCoord) throws TileStoreException {
    return cache.get(tileCoord, t -> {
      try {
        return tileStore.read(t);
      } catch (TileStoreException e) {
        logger.error("Unable to read the tile.", e);
        return null;
      }
    });
  }

  /** {@inheritDoc} */
  @Override
  public void write(TileCoord tileCoord, BufferedImage bufferedImage) throws TileStoreException {
    tileStore.write(tileCoord, bufferedImage);
    cache.invalidate(tileCoord);
  }

  /** {@inheritDoc} */
  @Override
  public void delete(TileCoord tileCoord) throws TileStoreException {
    tileStore.delete(tileCoord);
    cache.invalidate(tileCoord);
  }

  /** {@inheritDoc} */
  @Override
  public void close() throws Exception {
    tileStore.close();
    cache.cleanUp();
  }
}
