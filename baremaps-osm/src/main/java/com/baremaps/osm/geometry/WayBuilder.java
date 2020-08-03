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

package com.baremaps.osm.geometry;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.CacheException;
import com.baremaps.osm.model.Way;
import javax.inject.Inject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * A {@code WayBuilder} builds JTS linestring or polygons from OSM ways.
 */
public class WayBuilder extends GeometryBuilder<Way> {

  private final GeometryFactory geometryFactory;

  private final Cache<Long, Coordinate> coordinateCache;

  /**
   * Constructs a {code WayGeometryBuilder}.
   *
   * @param geometryFactory the {@code GeometryFactory} used to create geometries
   * @param coordinateCache the {@code Store} used to retrieve the coordinates of a node
   */
  @Inject
  public WayBuilder(GeometryFactory geometryFactory, Cache<Long, Coordinate> coordinateCache) {
    this.geometryFactory = geometryFactory;
    this.coordinateCache = coordinateCache;
  }

  /**
   * Builds a JTS linestring or polygons from OSM way.
   *
   * @param entity an OSM way
   * @return a JTS linestring or polygons corresponding to the way
   */
  public Geometry build(Way entity) {
    try {
      Coordinate[] coords = coordinateCache.getAll(entity.getNodes()).stream().toArray(Coordinate[]::new);
      if (coords.length > 3 && coords[0].equals(coords[coords.length - 1])) {
        return geometryFactory.createPolygon(coords);
      } else if (coords.length > 1) {
        return geometryFactory.createLineString(coords);
      } else {
        return null;
      }
    } catch (CacheException e) {
      return null;
    }
  }
}
