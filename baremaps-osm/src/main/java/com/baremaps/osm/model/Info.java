/*
 * Copyright (C) 2011 The Baremaps Authors
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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.time.LocalDateTime;
import java.util.Map;

public final class Info {

  private final long id;
  private final int version;
  private final LocalDateTime timestamp;
  private final long changeset;
  private final int userId;
  private final Map<String, String> tags;

  public Info(
      long id,
      int version,
      LocalDateTime timestamp,
      long changeset,
      int userId,
      Map<String, String> tags) {
    this.id = id;
    this.version = version;
    this.timestamp = timestamp;
    this.changeset = changeset;
    this.userId = userId;
    this.tags = tags;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Info info = (Info) o;
    return id == info.id
        && version == info.version
        && changeset == info.changeset
        && Objects.equal(timestamp, info.timestamp)
        && Objects.equal(userId, info.userId)
        && Objects.equal(tags, info.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, version, timestamp, changeset, userId, tags);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .toString();
  }
}
