package com.baremaps.tile;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.Weigher;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TileStore decorator that uses caffeine to cache the content of tiles.
 */
public class TileCache implements TileStore {

  private static final Logger logger = LoggerFactory.getLogger(TileCache.class);

  private final TileStore tileStore;

  private final Cache<Tile, byte[]> cache;

  /**
   * Decorates the TileStore with a cache.
   * @param tileStore
   * @param spec
   */
  public TileCache(TileStore tileStore, CaffeineSpec spec) {
    this.tileStore = tileStore;
    this.cache = Caffeine.from(spec).weigher(new Weigher<Tile, byte[]>() {
      @Override
      public @NonNegative int weigh(@NonNull Tile tile, byte @NonNull [] bytes) {
        return 28 + bytes.length;
      }
    }).build();
  }

  @Override
  public byte[] read(Tile tile) throws TileStoreException {
    return cache.get(tile, t -> {
      try {
        return tileStore.read(t);
      } catch (TileStoreException e) {
        logger.error("Unable to read the tile.", e);
        return null;
      }
    });
  }

  @Override
  public void write(Tile tile, byte[] bytes) throws TileStoreException {
    tileStore.write(tile, bytes);
    cache.invalidate(tile);
  }

  @Override
  public void delete(Tile tile) throws TileStoreException {
    tileStore.delete(tile);
    cache.invalidate(tile);
  }
}
