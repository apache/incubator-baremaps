/*
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

package org.apache.baremaps.openstreetmap.function;

import static org.apache.baremaps.utils.GeometryUtils.GEOMETRY_FACTORY;

import java.util.List;
import java.util.function.Function;
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.openstreetmap.model.Way;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A function that adds a geometry to a way.
 */
public record WayGeometryMapper(LongDataMap<Coordinate> coordinateMap, LongDataMap<List<Long>> referenceMap) implements Function<Way, Way> {

  private static final Logger logger = LoggerFactory.getLogger(WayGeometryMapper.class);

  /** {@inheritDoc} */
  @Override
  public Way apply(Way way) {
    try {
      List<Coordinate> list = way.nodes().stream().map(coordinateMap::get).toList();
      Coordinate[] array = list.toArray(new Coordinate[list.size()]);
      LineString line = GEOMETRY_FACTORY.createLineString(array);
      if (!line.isEmpty()) {
        if (!line.isClosed()) {
          return way.withGeometry(line);
        } else {
          Polygon polygon = GEOMETRY_FACTORY.createPolygon(line.getCoordinates());
          return way.withGeometry(polygon);
        }
      }
    } catch (Exception e) {
      logger.warn("Unable to build the geometry for way #" + way.id(), e);
    }
    return way;
  }
}
