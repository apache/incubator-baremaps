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
import org.locationtech.jts.geom.Geometry;

public final class Relation extends Entity {

  private final List<Member> members;

  private final Geometry geometry;

  public Relation(long id, int version, LocalDateTime timestamp, long changeset, int userId,
      Map<String, String> tags, List<Member> members) {
    this(id, version, timestamp, changeset, userId, tags, members, null);
  }

  public Relation(long id, int version, LocalDateTime timestamp, long changeset, int userId,
      Map<String, String> tags, List<Member> members, Geometry geometry) {
    super(id, version, timestamp, changeset, userId, tags);
    this.members = members;
    this.geometry = geometry;
  }

  public List<Member> getMembers() {
    return members;
  }

  public Optional<Geometry> getGeometry() {
    return Optional.ofNullable(geometry);
  }

}
