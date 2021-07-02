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

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import org.locationtech.jts.geom.Geometry;

/**
 * Represents an element in an OpenStreetMap dataset.
 * Elements are a basis to model the physical world.
 */
public abstract class Element implements Entity {

  protected final long id;

  protected final Info info;

  protected final Map<String, String> tags;

  protected Geometry geometry;

  protected Element(
      long id,
      Info info,
      Map<String, String> tags) {
    this(id, info, tags, null);
  }

  public Element(
      long id,
      Info info,
      Map<String, String> tags,
      Geometry geometry) {
    this.id = id;
    this.info = info;
    this.tags = tags;
    this.geometry = geometry;
  }

  public long getId() {
    return id;
  }

  public Info getInfo() {return info; }

  public Map<String, String> getTags() {
    return tags;
  }

  public Geometry getGeometry() {
    return this.geometry;
  }

  public void setGeometry(Geometry geometry) {
    this.geometry = geometry;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Element)) {
      return false;
    }
    Element element = (Element) o;
    return id == element.id &&
        Objects.equals(info, element.info) &&
        Objects.equals(tags, element.tags) &&
        Objects.equals(geometry, element.geometry);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, info, tags, geometry);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Element.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("info=" + info)
        .add("tags=" + tags)
        .add("geometry=" + geometry)
        .toString();
  }
}
