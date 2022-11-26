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

package org.apache.baremaps.openstreetmap.model;


import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;

/** Represents a relation element in an OpenStreetMap dataset. */
public record Relation(long id, Info info, Map<String, String> tags, List<Member> members, Geometry geometry)
  implements Element<Relation> {

  /**
   * Constructs an OpenStreetMap {@code Relation} with the specified parameters.
   *
   * @param id      the id
   * @param info    the information
   * @param tags    the tags
   * @param members the members
   */
  public Relation(long id, Info info, Map<String, String> tags, List<Member> members) {
    this(id, info, tags, members, null);
  }

  /** {@inheritDoc} */
  @Override
  public Relation withGeometry(Geometry geometry) {
    return new Relation(id, info, tags, members, geometry);
  }
}
