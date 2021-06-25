package com.baremaps.osm.cache;

import com.baremaps.osm.cache.Cache.Entry;
import com.baremaps.osm.domain.DataBlock;
import com.baremaps.osm.handler.BlockHandlerAdapter;
import java.util.List;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;

public class CacheImporter implements BlockHandlerAdapter {

  private final Cache<Long, Coordinate> coordiateCache;
  private final Cache<Long, List<Long>> referenceCache;

  public CacheImporter(
      Cache<Long, Coordinate> coordiateCache,
      Cache<Long, List<Long>> referenceCache) {
    this.coordiateCache = coordiateCache;
    this.referenceCache = referenceCache;
  }

  @Override
  public void handle(DataBlock dataBlock) throws Exception {
    coordiateCache.add(dataBlock.getDenseNodes().stream()
        .map(node -> new Entry<>(node.getId(), new Coordinate(node.getLon(), node.getLat())))
        .collect(Collectors.toList()));
    coordiateCache.add(dataBlock.getNodes().stream()
        .map(node -> new Entry<>(node.getId(), new Coordinate(node.getLon(), node.getLat())))
        .collect(Collectors.toList()));
    referenceCache.add(dataBlock.getWays().stream()
        .map(way -> new Entry<>(way.getId(), way.getNodes()))
        .collect(Collectors.toList()));
  }
}
