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

import java.util.function.Consumer;
import org.apache.baremaps.openstreetmap.model.Node;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 * A consumer that builds and sets a node geometry via side effects.
 */
public class NodeGeometryBuilder implements Consumer<Node> {

  /** {@inheritDoc} */
  @Override
  public void accept(Node node) {
    Point point = GEOMETRY_FACTORY_WGS84.createPoint(new Coordinate(node.getLon(), node.getLat()));
    node.setGeometry(point);
  }
}
