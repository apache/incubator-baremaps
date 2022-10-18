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

package org.apache.baremaps.osm.function;



import java.util.function.Function;
import org.apache.baremaps.osm.model.Bound;
import org.apache.baremaps.osm.model.Entity;
import org.apache.baremaps.osm.model.Header;
import org.apache.baremaps.osm.model.Node;
import org.apache.baremaps.osm.model.Relation;
import org.apache.baremaps.osm.model.Way;
import org.apache.baremaps.stream.StreamException;

/**
 * Represents a function that transforms entities of different types.
 *
 * @param <T>
 */
public interface EntityFunction<T> extends Function<Entity, T> {

  /** {@inheritDoc} */
  @Override
  default T apply(Entity entity) {
    try {
      return entity.visit(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  /**
   * Applies a function on a {@code Header}.
   *
   * @param header the header
   * @return the function result
   * @throws Exception
   */
  T match(Header header) throws Exception;

  /**
   * Applies a function on a {@code Bound}.
   *
   * @param bound the bound
   * @return the function result
   * @throws Exception
   */
  T match(Bound bound) throws Exception;

  /**
   * Applies a function on a {@code Node}.
   *
   * @param node the node
   * @return the function result
   * @throws Exception
   */
  T match(Node node) throws Exception;

  /**
   * Applies a function on a {@code Way}.
   *
   * @param way the way
   * @return the function result
   * @throws Exception
   */
  T match(Way way) throws Exception;

  /**
   * Applies a function on a {@code Relation}.
   *
   * @param relation the relation
   * @return the function result
   * @throws Exception
   */
  T match(Relation relation) throws Exception;
}
