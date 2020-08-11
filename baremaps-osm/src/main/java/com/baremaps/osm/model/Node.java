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
import java.util.Optional;
import java.util.StringJoiner;
import org.locationtech.jts.geom.Geometry;

public final class Node extends Entity {

  private double lon;

  private double lat;

  private Geometry geometry;

  @Override
  public String toString() {
    return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("version=" + version)
        .add("timestamp=" + timestamp)
        .add("changeset=" + changeset)
        .add("userId=" + userId)
        .add("tags=" + tags)
        .add("lon=" + lon)
        .add("lat=" + lat)
        .add("geometry=" + geometry)
        .toString();
  }

  public Node() {

  }

  public Node(long id, int version, LocalDateTime timestamp, long changeset, int userId,
      Map<String, String> tags, double lon, double lat) {
    this(id, version, timestamp, changeset, userId, tags, lon, lat, null);
  }

  public Node(long id, int version, LocalDateTime timestamp, long changeset, int userId,
      Map<String, String> tags, double lon, double lat, Geometry geometry) {
    super(id, version, timestamp, changeset, userId, tags);
    this.lon = lon;
    this.lat = lat;
    this.geometry = geometry;
  }

  public double getLon() {
    return lon;
  }

  public double getLat() {
    return lat;
  }

  public Optional<Geometry> getGeometry() {
    return Optional.ofNullable(geometry);
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public void setLat(double lat) {
    this.lat = lat;
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
    Node node = (Node) o;
    return Double.compare(node.lon, lon) == 0 &&
        Double.compare(node.lat, lat) == 0 &&
        Objects.equal(geometry, node.geometry);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), lon, lat, geometry);
  }
}
