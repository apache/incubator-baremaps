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

package com.baremaps.osm.cache;

import com.baremaps.osm.cache.Cache.Entry;
import com.baremaps.osm.domain.DataBlock;
import com.baremaps.osm.handler.BlockConsumerAdapter;
import java.util.List;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;

public class CacheBlockConsumer implements BlockConsumerAdapter {

  private final Cache<Long, Coordinate> coordiateCache;
  private final Cache<Long, List<Long>> referenceCache;

  public CacheBlockConsumer(
      Cache<Long, Coordinate> coordiateCache, Cache<Long, List<Long>> referenceCache) {
    this.coordiateCache = coordiateCache;
    this.referenceCache = referenceCache;
  }

  @Override
  public void match(DataBlock dataBlock) throws Exception {
    coordiateCache.add(
        dataBlock.getDenseNodes().stream()
            .map(node -> new Entry<>(node.getId(), new Coordinate(node.getLon(), node.getLat())))
            .collect(Collectors.toList()));
    coordiateCache.add(
        dataBlock.getNodes().stream()
            .map(node -> new Entry<>(node.getId(), new Coordinate(node.getLon(), node.getLat())))
            .collect(Collectors.toList()));
    referenceCache.add(
        dataBlock.getWays().stream()
            .map(way -> new Entry<>(way.getId(), way.getNodes()))
            .collect(Collectors.toList()));
  }
}
