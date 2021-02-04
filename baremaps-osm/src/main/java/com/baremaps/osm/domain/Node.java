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
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import org.locationtech.jts.geom.Geometry;

/**
 * A class used to represent a single point in space.
 */
public final class Node extends Element {

  private final double lon;

  private final double lat;

  public Node(long id, Info info, Map<String, String> tags, double lon, double lat) {
    super(id, info, tags);
    this.lon = lon;
    this.lat = lat;
  }

  public Node(long id, Info info, Map<String, String> tags, double lon, double lat, Geometry geometry) {
    super(id, info, tags, geometry);
    this.lon = lon;
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public double getLat() {
    return lat;
  }

  @Override
  public void accept(EntityHandler handler) throws Exception {
    handler.handle(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Node)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Node node = (Node) o;
    return Double.compare(node.lon, lon) == 0 &&
        Double.compare(node.lat, lat) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), lon, lat);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
        .add("lon=" + lon)
        .add("lat=" + lat)
        .add("id=" + id)
        .toString();
  }
}
