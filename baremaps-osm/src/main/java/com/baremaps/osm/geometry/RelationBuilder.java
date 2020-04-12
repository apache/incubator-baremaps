/*
 * Copyright (C) 2011 The Baremaps Authors
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

import com.baremaps.core.stream.Try;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.cache.Cache;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.proj4j.CoordinateTransform;

/**
 * A {@code RelationBuilder} builds JTS polygons and multipolygons from OSM relations.
 */
public class RelationBuilder extends GeometryBuilder<Relation> {

  private final GeometryFactory geometryFactory;

  private final Cache<Long, Coordinate> coordinateCache;

  private final Cache<Long, List<Long>> referenceCache;

  /**
   * Constructs a {@code RelationBuilder}.
   *
   * @param coordinateTransform the {@code CoordinateTransform} used to project OSM coordinates.
   * @param geometryFactory     the {@code GeometryFactory} used to create polygons and multipolygons
   * @param coordinateCache     the {@code Store} used to retrieve the coordinates of a node
   * @param referenceCache      the {@code Store} used to retrieve the nodes of a way
   */
  public RelationBuilder(CoordinateTransform coordinateTransform, GeometryFactory geometryFactory,
      Cache<Long, Coordinate> coordinateCache, Cache<Long, List<Long>> referenceCache) {
    super(coordinateTransform);
    this.geometryFactory = geometryFactory;
    this.coordinateCache = coordinateCache;
    this.referenceCache = referenceCache;
  }

  /**
   * Builds a JTS geometry from an OSM relation
   *
   * @param entity an OSM relation
   * @return a JTS polygon or multipolygon corresponding to the relation
   */
  public Geometry build(Relation entity) {
    // Check whether the relation is a multipolygon
    Map<String, String> tags = entity.getInfo().getTags();
    if (!"multipolygon".equals(tags.get("type"))) {
      return null;
    }

    // Collect the members of the relation
    List<LineString> members = entity.getMembers()
        .stream()
        .map(member -> referenceCache.get(member.getRef()))
        .filter(reference -> reference != null)
        .map(reference -> Try.of(() -> coordinateCache.getAll(reference).stream()
            .filter(coordinate -> coordinate != null)
            .map(coordinate -> toCoordinate(coordinate.getX(), coordinate.getY()))
            .toArray(Coordinate[]::new)))
        .filter(t -> t.isSuccess())
        .map(t -> t.value())
        .map(t -> geometryFactory.createLineString(t))
        .collect(Collectors.toList());

    // Check whether the relation contains members
    if (members.isEmpty()) {
      return null;
    }

    // Try to create the polygon from the members
    try {
      Polygonizer polygonizer = new Polygonizer(true);
      polygonizer.add(members);
      return polygonizer.getGeometry();
    } catch (Exception ex) {
      return null;
    }
  }

}
