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
import org.apache.baremaps.openstreetmap.model.Node;
import org.locationtech.jts.geom.Coordinate;

/**
 * A function that adds a geometry to a node.
 */
public record NodeGeometryMapper(LongDataMap<Coordinate> coordinateMap, LongDataMap<List<Long>> referenceMap) implements Function<Node, Node> {

  /** {@inheritDoc} */
  @Override
  public Node apply(Node node) {
    var point = GEOMETRY_FACTORY.createPoint(new Coordinate(node.lon(), node.lat()));
    return node.withGeometry(point);
  }
}
