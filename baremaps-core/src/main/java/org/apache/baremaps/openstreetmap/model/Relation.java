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
public final class Relation extends Element {

  private final List<Member> members;

  /**
   * Constructs an OpenStreetMap {@code Relation} with the specified parameters.
   *
   * @param id the id
   * @param info the information
   * @param tags the tags
   * @param members the members
   */
  public Relation(long id, Info info, Map<String, String> tags, List<Member> members) {
    super(id, info, tags);
    this.members = members;
  }

  /**
   * Constructs an OpenStreetMap {@code Relation} with the specified parameters.
   *
   * @param id the id
   * @param info the information
   * @param tags the tags
   * @param members the members
   * @param geometry the geometry
   */
  public Relation(long id, Info info, Map<String, String> tags, List<Member> members,
      Geometry geometry) {
    super(id, info, tags, geometry);
    this.members = members;
  }

  /**
   * Returns the members.
   *
   * @return the members
   */
  public List<Member> members() {
    return members;
  }
}
