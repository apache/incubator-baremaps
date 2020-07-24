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
import com.baremaps.osm.geometry.NodeGeometryBuilder;
import com.baremaps.osm.pbf.FileBlock;
import com.baremaps.osm.pbf.FileBlock.Type;
import com.baremaps.osm.pbf.PrimitiveBlock;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;

public class CacheImporter implements Consumer<FileBlock> {

  private final NodeGeometryBuilder nodeGeometryBuilder;
  private final Cache<Long, Coordinate> coordinateCache;
  private final Cache<Long, List<Long>> referenceCache;

  public CacheImporter(
      NodeGeometryBuilder nodeGeometryBuilder,
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referenceCache) {
    this.nodeGeometryBuilder = nodeGeometryBuilder;
    this.coordinateCache = coordinateCache;
    this.referenceCache = referenceCache;
  }

  @Override
  public void accept(FileBlock block) {
    if (block.getType().equals(Type.OSMData)) {
      PrimitiveBlock primitiveBlock = block.toPrimitiveBlock();
      coordinateCache.putAll(primitiveBlock.getDenseNodes().stream()
          .map(n -> new Entry<>(n.getId(), nodeGeometryBuilder.build(n).getCoordinate()))
          .collect(Collectors.toList()));
      coordinateCache.putAll(primitiveBlock.getNodes().stream()
          .map(n -> new Entry<>(n.getId(), nodeGeometryBuilder.build(n).getCoordinate()))
          .collect(Collectors.toList()));
      referenceCache.putAll(primitiveBlock.getWays().stream()
          .map(w -> new Entry<>(w.getId(), w.getNodes()))
          .collect(Collectors.toList()));
    }
  }

}
