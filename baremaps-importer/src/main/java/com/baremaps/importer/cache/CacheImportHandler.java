package com.baremaps.importer.cache;

import com.baremaps.importer.cache.Cache.Entry;
import com.baremaps.osm.BlockHandler;
import com.baremaps.osm.DefaultBlockHandler;
import com.baremaps.osm.EntityHandler;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.pbf.DataBlock;
import com.baremaps.osm.pbf.HeaderBlock;
import java.util.List;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;

public class CacheImportHandler implements DefaultBlockHandler {

  private final Cache<Long, Coordinate> coordiateCache;
  private final Cache<Long, List<Long>> referenceCache;

  public CacheImportHandler(
      Cache<Long, Coordinate> coordiateCache,
      Cache<Long, List<Long>> referenceCache) {
    this.coordiateCache = coordiateCache;
    this.referenceCache = referenceCache;
  }

  @Override
  public void handle(DataBlock dataBlock) throws Exception {
    coordiateCache.putAll(dataBlock.getDenseNodes().stream()
        .map(node -> new Entry<>(node.getId(), new Coordinate(node.getLon(), node.getLat())))
        .collect(Collectors.toList()));
    coordiateCache.putAll(dataBlock.getNodes().stream()
        .map(node -> new Entry<>(node.getId(), new Coordinate(node.getLon(), node.getLat())))
        .collect(Collectors.toList()));
    referenceCache.putAll(dataBlock.getWays().stream()
        .map(way -> new Entry<>(way.getId(), way.getNodes()))
        .collect(Collectors.toList()));
  }
}
