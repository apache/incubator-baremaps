package com.baremaps.importer.geometry;

import com.baremaps.importer.cache.Cache;
import com.baremaps.importer.cache.CacheImporter;
import com.baremaps.importer.cache.InMemoryCache;
import com.baremaps.osm.ChangeHandler;
import com.baremaps.osm.model.Change;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

public class ChangeGeometryBuilder implements ChangeHandler {

  protected final GeometryFactory geometryFactory;

  public ChangeGeometryBuilder(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public void handle(Change change) {
    Cache<Long, Coordinate> coordinateCache = new InMemoryCache<>();
    Cache<Long, List<Long>> referencesCache = new InMemoryCache<>();
    change.getElements().forEach(new CacheImporter(coordinateCache, referencesCache));
    change.getElements().forEach(new GeometryBuilder(geometryFactory, coordinateCache, referencesCache));
  }

}
