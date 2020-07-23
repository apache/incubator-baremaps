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
package com.baremaps.osm.store;

import com.google.common.base.Objects;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

public class WayEntity {

  private final long id;

  private final int version;

  private final LocalDateTime timestamp;

  private final long changeset;

  private final int userId;

  private final Map<String, String> tags;

  private final List<Long> nodes;

  private final Geometry geometry;

  public WayEntity(
      long id,
      int version,
      LocalDateTime timestamp,
      long changeset,
      int userId,
      Map<String, String> tags,
      List<Long> nodes,
      Geometry geometry) {
    this.id = id;
    this.version = version;
    this.timestamp = timestamp;
    this.changeset = changeset;
    this.userId = userId;
    this.tags = tags;
    this.nodes = nodes;
    this.geometry = geometry;
  }

  public long getId() {
    return id;
  }

  public int getVersion() {
    return version;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public long getChangeset() {
    return changeset;
  }

  public int getUserId() {
    return userId;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public List<Long> getNodes() {
    return nodes;
  }

  public Geometry getGeometry() {
    return geometry;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WayEntity way = (WayEntity) o;
    return id == way.id &&
        version == way.version &&
        changeset == way.changeset &&
        userId == way.userId &&
        Objects.equal(timestamp, way.timestamp) &&
        Objects.equal(tags, way.tags) &&
        Objects.equal(nodes, way.nodes) &&
        Objects.equal(geometry, way.geometry);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, version, timestamp, changeset, userId, tags, nodes, geometry);
  }
}
