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

import com.baremaps.osm.model.Bound;
import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;

/** {@inheritDoc} */
public interface EntityConsumerAdapter extends EntityConsumer {

  /** {@inheritDoc} */
  default void match(Header header) throws Exception {}

  /** {@inheritDoc} */
  default void match(Bound bound) throws Exception {}

  /** {@inheritDoc} */
  default void match(Node node) throws Exception {}

  /** {@inheritDoc} */
  default void match(Way way) throws Exception {}

  /** {@inheritDoc} */
  default void match(Relation relation) throws Exception {}
}
