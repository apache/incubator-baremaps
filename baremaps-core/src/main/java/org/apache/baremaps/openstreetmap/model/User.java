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



import com.google.common.base.Objects;

/** Represents the author of an objet in an OpenStreetMap dataset. */
public final class User {

  public static final User NO_USER = new User(-1, "");

  private final int id;
  private final String name;

  /**
   * Constructs an OpenStreetMap {@code User} with the specified parameters.
   *
   * @param id the id
   * @param name the name
   */
  public User(int id, String name) {
    this.id = id;
    this.name = name;
  }

  /**
   * Returns the id.
   *
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return id == user.id && Objects.equal(name, user.name);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hashCode(id, name);
  }
}
