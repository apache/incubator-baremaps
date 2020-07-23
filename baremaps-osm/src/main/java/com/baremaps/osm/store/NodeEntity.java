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
import java.util.Map;
import org.locationtech.jts.geom.Point;

public class NodeEntity {

  private final long id;

  private final int version;

  private final LocalDateTime timestamp;

  private final long changeset;

  private final int userId;

  private final Map<String, String> tags;

  private final Point geometry;

  public NodeEntity(
      long id,
      int version,
      LocalDateTime timestamp,
      long changeset,
      int userId,
      Map<String, String> tags,
      Point geometry) {
    this.id = id;
    this.version = version;
    this.timestamp = timestamp;
    this.changeset = changeset;
    this.userId = userId;
    this.tags = tags;
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

  public Point getPoint() {
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
    NodeEntity node = (NodeEntity) o;
    return id == node.id &&
        version == node.version &&
        changeset == node.changeset &&
        userId == node.userId &&
        Objects.equal(timestamp, node.timestamp) &&
        Objects.equal(tags, node.tags) &&
        Objects.equal(geometry, node.geometry);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, version, timestamp, changeset, userId, tags, geometry);
  }
}
