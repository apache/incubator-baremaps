package com.baremaps.importer.cache;

import com.baremaps.osm.DefaultEntityHandler;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Way;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.locationtech.jts.geom.Coordinate;

public class CacheImportHandler implements Consumer<Stream<Entity>> {

  private final Cache<Long, Coordinate> coordiateCache;
  private final Cache<Long, List<Long>> referenceCache;

  public CacheImportHandler(
      Cache<Long, Coordinate> coordiateCache,
      Cache<Long, List<Long>> referenceCache) {
    this.coordiateCache = coordiateCache;
    this.referenceCache = referenceCache;
  }

  @Override
  public void accept(Stream<Entity> entities) {
    entities.forEach(new DefaultEntityHandler() {
      @Override
      public void handle(Node node) {
        coordiateCache.put(node.getId(), new Coordinate(node.getLon(), node.getLat()));
      }

      @Override
      public void handle(Way way) {
        referenceCache.put(way.getId(), way.getNodes());
      }
    });
  }
}
