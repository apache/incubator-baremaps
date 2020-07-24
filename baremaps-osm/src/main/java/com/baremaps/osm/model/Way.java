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

import com.google.common.base.Objects;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import org.locationtech.jts.geom.Geometry;

public final class Way extends Entity {

  private List<Long> nodes;

  private Geometry geometry;

  public Way() {

  }

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

  public void setNodes(List<Long> nodes) {
    this.nodes = nodes;
  }

  public void setGeometry(Geometry geometry) {
    this.geometry = geometry;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Way way = (Way) o;
    return Objects.equal(nodes, way.nodes) &&
        Objects.equal(geometry, way.geometry);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), nodes, geometry);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Way.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("version=" + version)
        .add("timestamp=" + timestamp)
        .add("changeset=" + changeset)
        .add("userId=" + userId)
        .add("tags=" + tags)
        .add("nodes=" + nodes)
        .add("geometry=" + geometry)
        .toString();
  }
}
