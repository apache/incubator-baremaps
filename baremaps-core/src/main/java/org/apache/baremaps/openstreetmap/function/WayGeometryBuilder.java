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

import static org.apache.baremaps.utils.GeometryUtils.GEOMETRY_FACTORY_WGS84;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.baremaps.database.collection.DataMap;
import org.apache.baremaps.openstreetmap.model.Way;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A consumer that builds and sets a way geometry via side effects.
 */
public class WayGeometryBuilder implements Consumer<Way> {

  private static final Logger logger = LoggerFactory.getLogger(WayGeometryBuilder.class);

  private final DataMap<Long, Coordinate> coordinateMap;

  /**
   * Constructs a way geometry builder.
   *
   * @param coordinateMap the coordinates map
   */
  public WayGeometryBuilder(DataMap<Long, Coordinate> coordinateMap) {
    this.coordinateMap = coordinateMap;
  }

  /** {@inheritDoc} */
  @Override
  public void accept(Way way) {
    try {
      // Build the coordinate list and remove duplicates.
      List<Coordinate> list = new ArrayList<>();
      Coordinate previous = null;
      for (Long id : way.getNodes()) {
        Coordinate coordinate = coordinateMap.get(id);
        if (coordinate != null && !coordinate.equals(previous)) {
          list.add(coordinate);
          previous = coordinate;
        }
      }

      Coordinate[] array = list.toArray(new Coordinate[0]);
      LineString line = GEOMETRY_FACTORY_WGS84.createLineString(array);

      if (!line.isEmpty()) {
        // Ways can be open or closed depending on the geometry or the tags:
        // https://wiki.openstreetmap.org/wiki/Way
        if (!line.isClosed()
            || way.getTags().containsKey("railway")
            || way.getTags().containsKey("highway")
            || way.getTags().containsKey("barrier")) {
          way.setGeometry(line);
        } else {
          Polygon polygon = GEOMETRY_FACTORY_WGS84.createPolygon(line.getCoordinates());
          if (polygon.isValid()) {
            way.setGeometry(polygon);
          } else {
            var geometryFixer = new GeometryFixer(polygon);
            var fixedGeometry = geometryFixer.getResult();
            way.setGeometry(fixedGeometry);
          }
        }
      }
    } catch (Exception e) {
      logger.debug("Unable to build the geometry for way #" + way.getId(), e);
      way.setGeometry(GEOMETRY_FACTORY_WGS84.createEmpty(0));
    }
  }
}
