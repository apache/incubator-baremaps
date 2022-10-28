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



import java.util.function.Consumer;
import org.apache.baremaps.openstreetmap.model.Change;
import org.apache.baremaps.stream.StreamException;

/** Represents an operation on changes of different types. */
public interface ChangeConsumer extends Consumer<Change> {

  /** {@inheritDoc} */
  @Override
  default void accept(Change change) {
    try {
      change.visit(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  /**
   * Matches an operation on a {@code Change}.
   *
   * @param change the change
   * @throws Exception
   */
  void match(Change change) throws Exception;
}
