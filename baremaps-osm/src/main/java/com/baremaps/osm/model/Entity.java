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
import java.util.Map;

/**
 * An entity in the OSM dataset.
 */
public abstract class Entity {

  protected long id;
  protected int version;
  protected LocalDateTime timestamp;
  protected long changeset;
  protected int userId;
  protected Map<String, String> tags;

  protected Entity() {

  }

  protected Entity(long id, int version, LocalDateTime timestamp, long changeset, int userId,
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

  public void setId(long id) {
    this.id = id;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public void setChangeset(long changeset) {
    this.changeset = changeset;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Entity entity = (Entity) o;
    return id == entity.id &&
        version == entity.version &&
        changeset == entity.changeset &&
        userId == entity.userId &&
        Objects.equal(timestamp, entity.timestamp) &&
        Objects.equal(tags, entity.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, version, timestamp, changeset, userId, tags);
  }


}
