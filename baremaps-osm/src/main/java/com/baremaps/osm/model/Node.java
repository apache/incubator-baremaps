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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public final class Node implements Entity {

  private final Info info;

  private final double lon;

  private final double lat;

  public Node(Info info, double lon, double lat) {
    checkNotNull(info);
    this.info = info;
    this.lon = lon;
    this.lat = lat;
  }

  @Override
  public Info getInfo() {
    return info;
  }

  public double getLon() {
    return lon;
  }

  public double getLat() {
    return lat;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Node node = (Node) o;
    return Double.compare(node.lon, lon) == 0
        && Double.compare(node.lat, lat) == 0
        && Objects.equal(info, node.info);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(info, lon, lat);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("info", info)
        .add("lon", lon)
        .add("lat", lat)
        .toString();
  }

}
