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

package com.baremaps.osm.store;

import com.baremaps.osm.domain.DataBlock;
import com.baremaps.osm.function.BlockConsumerAdapter;
import com.baremaps.store.LongDataMap;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;

/**
 * A consumer that stores osm nodes and ways in the provided caches.
 */
public class DataStoreConsumer implements BlockConsumerAdapter {

  private final LongDataMap<Coordinate> coordiateCache;
  private final LongDataMap<List<Long>> referenceCache;

  /**
   * Constructs a {@code CacheBlockConsumer} with the provided caches.
   *
   * @param coordiateCache the cache of coordinates
   * @param referenceCache the cache of references
   */
  public DataStoreConsumer(
      LongDataMap<Coordinate> coordiateCache, LongDataMap<List<Long>> referenceCache) {
    this.coordiateCache = coordiateCache;
    this.referenceCache = referenceCache;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void match(DataBlock dataBlock) throws Exception {
    dataBlock.getDenseNodes().stream()
        .forEach(node -> coordiateCache.put(node.getId(), new Coordinate(node.getLon(), node.getLat())));
    dataBlock.getNodes().stream()
        .forEach(node -> coordiateCache.put(node.getId(), new Coordinate(node.getLon(), node.getLat())));
    dataBlock.getWays().stream()
        .forEach(way -> referenceCache.put(way.getId(), way.getNodes()));
  }
}
