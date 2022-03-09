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
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;

/** A function that maps an {@code Entity} to its {@code Geometry}. */
public class ExtractGeometryFunction implements EntityFunction<Optional<Geometry>> {

  /** {@inheritDoc} */
  @Override
  public Optional<Geometry> match(Header header) throws Exception {
    return Optional.empty();
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Geometry> match(Bound bound) throws Exception {
    return Optional.empty();
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Geometry> match(Node node) throws Exception {
    return Optional.ofNullable(node.getGeometry());
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Geometry> match(Way way) throws Exception {
    return Optional.ofNullable(way.getGeometry());
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Geometry> match(Relation relation) throws Exception {
    return Optional.ofNullable(relation.getGeometry());
  }
}
