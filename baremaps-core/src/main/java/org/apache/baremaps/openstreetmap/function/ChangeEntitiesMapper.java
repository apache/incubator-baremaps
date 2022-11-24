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



import java.util.function.Function;
import org.apache.baremaps.openstreetmap.model.Change;
import org.apache.baremaps.openstreetmap.model.Entity;

/** Represents an operation on the entities of changes of different types. */
public class ChangeEntitiesMapper implements Function<Change, Change> {

  private final Function<Entity, Entity> mapper;

  /**
   * Constructs a consumer that applies the specified consumer to all the entities of a {@code
   * Change}.
   *
   * @param mapper
   */
  public ChangeEntitiesMapper(Function<Entity, Entity> mapper) {
    this.mapper = mapper;
  }

  @Override
  public Change apply(Change change) {
    return new Change(change.type(), change.entities().stream().map(mapper).toList());
  }
}
