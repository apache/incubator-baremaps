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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

public class RelationEntity {

  private final long id;

  private final int version;

  private final LocalDateTime timestamp;

  private final long changeset;

  private final int userId;

  private final Map<String, String> tags;

  private final Long[] memberRefs;

  private final String[] memberTypes;

  private final String[] memberRoles;

  private final Geometry geometry;

  public RelationEntity(
      long id,
      int version,
      LocalDateTime timestamp,
      long changeset,
      int userId,
      Map<String, String> tags,
      Long[] memberRefs,
      String[] memberTypes,
      String[] memberRoles,
      Geometry geometry) {
    this.id = id;
    this.version = version;
    this.timestamp = timestamp;
    this.changeset = changeset;
    this.userId = userId;
    this.tags = tags;
    this.memberRefs = memberRefs;
    this.memberTypes = memberTypes;
    this.memberRoles = memberRoles;
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

  public Long[] getMemberRefs() {
    return memberRefs;
  }

  public String[] getMemberTypes() {
    return memberTypes;
  }

  public String[] getMemberRoles() {
    return memberRoles;
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
    RelationEntity relation = (RelationEntity) o;
    return id == relation.id &&
        version == relation.version &&
        changeset == relation.changeset &&
        userId == relation.userId &&
        Objects.equal(timestamp, relation.timestamp) &&
        Objects.equal(tags, relation.tags) &&
        Arrays.deepEquals(memberRefs, relation.memberRefs) &&
        Arrays.deepEquals(memberTypes, relation.memberTypes) &&
        Arrays.deepEquals(memberRoles, relation.memberRoles) &&
        Objects.equal(geometry, relation.geometry);
  }

  @Override
  public int hashCode() {
    return Objects
        .hashCode(id, version, timestamp, changeset, userId, tags, memberRefs, memberTypes, memberRoles,
            geometry);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("version", version)
        .add("timestamp", timestamp)
        .add("changeset", changeset)
        .add("userId", userId)
        .add("tags", tags)
        .add("memberRefs", memberRefs)
        .add("memberTypes", memberTypes)
        .add("memberRoles", memberRoles)
        .add("geometry", geometry)
        .toString();
  }
}
