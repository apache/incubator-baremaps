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
import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.reader.pbf.FileBlockHandler;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.locationtech.jts.geom.Coordinate;

public class CacheBuilder implements FileBlockHandler {

  private final NodeBuilder nodeBuilder;
  private final Cache<Long, Coordinate> coordinateCache;
  private final Cache<Long, List<Long>> referenceCache;

  @Inject
  public CacheBuilder(
      NodeBuilder nodeBuilder,
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referenceCache) {
    this.nodeBuilder = nodeBuilder;
    this.coordinateCache = coordinateCache;
    this.referenceCache = referenceCache;
  }

  @Override
  public void onHeader(Header header) {
    // do nothing
  }

  @Override
  public void onNodes(List<Node> nodes) {
    coordinateCache.putAll(nodes.stream()
        .map(n -> new Entry<>(n.getId(), nodeBuilder.build(n).getCoordinate()))
        .collect(Collectors.toList()));
  }

  @Override
  public void onWays(List<Way> ways) {
    referenceCache.putAll(ways.stream()
        .map(w -> new Entry<>(w.getId(), w.getNodes()))
        .collect(Collectors.toList()));
  }

  @Override
  public void onRelations(List<Relation> relations) {
    // do nothing
  }
}
