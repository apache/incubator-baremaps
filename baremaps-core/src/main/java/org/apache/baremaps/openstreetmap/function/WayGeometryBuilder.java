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

import static org.apache.baremaps.openstreetmap.utils.GeometryUtils.GEOMETRY_FACTORY_WGS84;

import java.util.List;
import java.util.function.Consumer;
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.openstreetmap.model.Way;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A consumer that builds and sets a way geometry via side effects.
 */
public class WayGeometryBuilder implements Consumer<Way> {

  private static final Logger logger = LoggerFactory.getLogger(WayGeometryBuilder.class);

  private final LongDataMap<Coordinate> coordinateMap;

  /**
   * Constructs a way geometry builder.
   *
   * @param coordinateMap the coordinates map
   */
  public WayGeometryBuilder(LongDataMap<Coordinate> coordinateMap) {
    this.coordinateMap = coordinateMap;
  }

  /** {@inheritDoc} */
  @Override
  public void accept(Way way) {
    try {
      List<Coordinate> list = way.getNodes().stream().map(coordinateMap::get).toList();
      Coordinate[] array = list.toArray(new Coordinate[list.size()]);
      LineString line = GEOMETRY_FACTORY_WGS84.createLineString(array);
      if (!line.isEmpty()) {
        if (!line.isClosed()) {
          way.setGeometry(line);
        } else {
          Polygon polygon = GEOMETRY_FACTORY_WGS84.createPolygon(line.getCoordinates());
          way.setGeometry(polygon);
        }
      }
    } catch (Exception e) {
      logger.warn("Unable to build the geometry for way #" + way.getId(), e);
    }
  }
}
