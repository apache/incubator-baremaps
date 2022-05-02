/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.osm.function;

import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.stream.StreamException;
import java.util.function.Consumer;

/** Represents an operation on entities of different types. */
public interface EntityConsumer extends Consumer<Entity> {

  /** {@inheritDoc} */
  @Override
  default void accept(Entity entity) {
    try {
      entity.visit(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  /**
   * Matches an operation on a {@code Header}.
   *
   * @param header the header
   * @throws Exception
   */
  void match(Header header) throws Exception;

  /**
   * Matches an operation on a {@code Bound}.
   *
   * @param bound the bound
   * @throws Exception
   */
  void match(Bound bound) throws Exception;

  /**
   * Matches an operation on a {@code Node}.
   *
   * @param node the node
   * @throws Exception
   */
  void match(Node node) throws Exception;

  /**
   * Matches an operation on a {@code Way}.
   *
   * @param way the way
   * @throws Exception
   */
  void match(Way way) throws Exception;

  /**
   * Matches an operation on a {@code Relation}.
   *
   * @param relation the relation
   * @throws Exception
   */
  void match(Relation relation) throws Exception;
}
