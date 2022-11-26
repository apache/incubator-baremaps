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



import java.util.Map;
import org.locationtech.jts.geom.Geometry;

/**
 * Represents an element in an OpenStreetMap dataset. Elements are a basis to model the physical
 * world.
 */
public sealed

interface Element<T> extends Entity
permits Node, Way, Relation
{

  /**
   * Returns the id.
   *
   * @return the id
   */
  long id();

  /**
   * Returns the info.
   *
   * @return the info
   */
  Info info();

  /**
   * Returns the tags.
   *
   * @return the tags
   */
  Map<String, String> tags();

  /**
   * Returns the geometry.
   *
   * @return the geometry
   */
  Geometry geometry();

  /**
   * Returns a new element with the provided the geometry.
   *
   * @param geometry the geometry
   */
  T withGeometry(Geometry geometry);

}
