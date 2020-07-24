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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * A {@code WayBuilder} builds JTS linestring or polygons from OSM ways.
 */
public class WayGeometryBuilder extends GeometryBuilder<Way> {

  private final GeometryFactory geometryFactory;

  private final Cache<Long, Coordinate> coordinateCache;

  /**
   * Constructs a {code WayBuilder}.
   *
   * @param geometryFactory the {@code GeometryFactory} used to create geometries
   * @param coordinateCache the {@code Store} used to retrieve the coordinates of a node
   */
  public WayGeometryBuilder(GeometryFactory geometryFactory, Cache<Long, Coordinate> coordinateCache) {
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
      Coordinate[] coordinates =
          coordinateCache.getAll(entity.getNodes()).stream()
              .toArray(Coordinate[]::new);
      if (coordinates.length > 3 && coordinates[0].equals(coordinates[coordinates.length - 1])) {
        return geometryFactory.createPolygon(coordinates);
      } else if (coordinates.length > 1) {
        return geometryFactory.createLineString(coordinates);
      } else {
        return null;
      }
    } catch (CacheException e) {
      return null;
    }
  }
}
