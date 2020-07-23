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

package com.baremaps.osm.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;

public final class Way extends Entity {

  private final List<Long> nodes;

  private final Geometry geometry;

  public Way(long id, int version, LocalDateTime timestamp, long changeset, int userId,
      Map<String, String> tags, List<Long> nodes) {
    this(id, version, timestamp, changeset, userId, tags, nodes, null);
  }

  public Way(long id, int version, LocalDateTime timestamp, long changeset, int userId,
      Map<String, String> tags, List<Long> nodes, Geometry geometry) {
    super(id, version, timestamp, changeset, userId, tags);
    this.nodes = nodes;
    this.geometry = geometry;
  }

  public List<Long> getNodes() {
    return nodes;
  }

  public Optional<Geometry> getGeometry() {
    return Optional.ofNullable(geometry);
  }

}
