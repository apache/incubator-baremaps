/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.openstreetmap.function;



import java.util.List;
import java.util.function.Consumer;
import org.apache.baremaps.database.collection.DataMap;
import org.apache.baremaps.openstreetmap.model.*;
import org.locationtech.jts.geom.*;

/** A consumer that builds and sets the geometry of OpenStreetMap entities via side effects. */
public class EntityGeometryBuilder implements Consumer<Entity> {

  private final Consumer<Node> nodeGeometryBuilder;
  private final Consumer<Way> wayGeometryBuilder;
  private final Consumer<Relation> relationMultiPolygonBuilder;

  /**
   * Constructs a consumer that uses the provided caches to create and set geometries.
   *
   * @param coordinateMap the coordinate cache
   * @param referenceMap the reference cache
   */
  public EntityGeometryBuilder(
      DataMap<Long, Coordinate> coordinateMap,
      DataMap<Long, List<Long>> referenceMap) {
    this.nodeGeometryBuilder = new NodeGeometryBuilder();
    this.wayGeometryBuilder = new WayGeometryBuilder(coordinateMap);
    this.relationMultiPolygonBuilder = new RelationMultiPolygonBuilder(coordinateMap, referenceMap);
  }

  /**
   * A default predicate that returns true if the relation is a multipolygon.
   */
  private static boolean isMultiPolygon(Relation relation) {
    var tags = relation.getTags();
    if ("coastline".equals(tags.get("natural"))) {
      // Coastlines are complex relations that we do not handle
      return false;
    } else {
      // MultiPolygons and boundaries are complex relations that we handle
      return "multipolygon".equals(tags.get("type")) || "boundary".equals(tags.get("type"));
    }
  }

  /** {@inheritDoc} */
  @Override
  public void accept(Entity entity) {
    if (entity instanceof Node node) {
      nodeGeometryBuilder.accept(node);
    } else if (entity instanceof Way way) {
      wayGeometryBuilder.accept(way);
    } else if (entity instanceof Relation relation && isMultiPolygon(relation)) {
      relationMultiPolygonBuilder.accept(relation);
    }
  }

}
