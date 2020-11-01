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

import com.baremaps.osm.EntityHandler;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

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
  public void visit(EntityHandler visitor) throws Exception {
    visitor.handle(this);
  }
}
