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
import java.util.StringJoiner;

/** Represents a change in an OpenStreetMap dataset. */
public final class Change {

  public enum ChangeType {
    DELETE, CREATE, MODIFY
  }

  private final ChangeType type;

  private final List<Entity> entities;

  /**
   * Constructs an OpenStreetMap change.
   *
   * @param type the type of the change
   * @param entities the entities affected by the change
   */
  public Change(ChangeType type, List<Entity> entities) {
    this.type = type;
    this.entities = entities;
  }

  /**
   * Returns the type of the change.
   *
   * @return the type of the change
   */
  public ChangeType getType() {
    return type;
  }

  /**
   * Returns the entities affected by the change.
   *
   * @return the entities affected by the change
   */
  public List<Entity> getEntities() {
    return entities;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return new StringJoiner(", ", Change.class.getSimpleName() + "[", "]").add("type=" + type)
        .add("elements=" + entities).toString();
  }
}
