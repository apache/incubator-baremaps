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

package com.baremaps.osm.domain;

import com.baremaps.osm.handler.EntityHandler;
import com.baremaps.osm.handler.EntityMapper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import org.locationtech.jts.geom.Geometry;

/**
 * A class used to represent groups of elements.
 */
public final class Relation extends Element {

  private final List<Member> members;

  public Relation(long id, Info info, Map<String, String> tags, List<Member> members) {
    super(id, info, tags);
    this.members = members;
  }

  public Relation(long id, Info info, Map<String, String> tags, List<Member> members, Geometry geometry) {
    super(id, info, tags, geometry);
    this.members = members;
  }

  public List<Member> getMembers() {
    return members;
  }

  @Override
  public void accept(EntityHandler handler) throws Exception {
    handler.handle(this);
  }

  @Override
  public <T> T accept(EntityMapper<T> mapper) throws Exception {
    return mapper.map(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Relation)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Relation relation = (Relation) o;
    return Objects.equals(members, relation.members);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), members);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Relation.class.getSimpleName() + "[", "]")
        .add("members=" + members)
        .add("id=" + id)
        .toString();
  }
}
