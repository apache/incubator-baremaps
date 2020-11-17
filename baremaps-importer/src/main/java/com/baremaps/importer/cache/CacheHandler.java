package com.baremaps.importer.cache;

import com.baremaps.importer.cache.Cache.Entry;
import com.baremaps.osm.EntityHandler;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.locationtech.jts.geom.Coordinate;

public class CacheHandler implements EntityHandler, AutoCloseable {

  private final Cache<Long, Coordinate> coordiateCache;
  private final Cache<Long, List<Long>> referenceCache;

  private final Map<Thread, List<Entry<Long, Coordinate>>> coordinateBuffers = new ConcurrentHashMap<>();
  private final Map<Thread, List<Entry<Long, List<Long>>>> referencesBuffers = new ConcurrentHashMap<>();

  public CacheHandler(
      Cache<Long, Coordinate> coordiateCache,
      Cache<Long, List<Long>> referenceCache) {
    this.coordiateCache = coordiateCache;
    this.referenceCache = referenceCache;
  }

  @Override
  public void handle(Header header) {
    // Do nothing
  }

  @Override
  public void handle(Bound bound) {
    // Do nothing
  }

  @Override
  public void handle(Node node) {
    List<Entry<Long, Coordinate>> buffer = coordinateBuffers.computeIfAbsent(Thread.currentThread(), thread -> new ArrayList<>());
    buffer.add(new Entry<>(node.getId(), new Coordinate(node.getLon(), node.getLat())));
    if (buffer.size() == 1000) {
      coordiateCache.putAll(buffer);
      buffer.clear();
    }
  }

  @Override
  public void handle(Way way) {
    List<Entry<Long, List<Long>>> buffer = referencesBuffers.computeIfAbsent(Thread.currentThread(), thread -> new ArrayList<>());
    buffer.add(new Entry<>(way.getId(), way.getNodes()));
    if (buffer.size() == 1000) {
      referenceCache.putAll(buffer);
      buffer.clear();
    }
  }

  @Override
  public void handle(Relation relation) {
    // Do nothing
  }

  @Override
  public void close() {
    for (List<Entry<Long, Coordinate>> buffer : coordinateBuffers.values()) {
      coordiateCache.putAll(buffer);
      buffer.clear();
    }
    for (List<Entry<Long, List<Long>>> buffer : referencesBuffers.values()) {
      referenceCache.putAll(buffer);
      buffer.clear();
    }
  }

}
