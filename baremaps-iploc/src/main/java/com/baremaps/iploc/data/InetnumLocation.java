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

package com.baremaps.iploc.data;

/** Contains an IP range along with its position in the world */
public class InetnumLocation {
  private String name;
  private Ipv4Range ipv4Range;
  private Location location;

  public InetnumLocation(String name, Ipv4Range ipv4Range, Location location) {
    this.name = name;
    this.ipv4Range = ipv4Range;
    this.location = location;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Ipv4Range getIpv4Range() {
    return ipv4Range;
  }

  public void setIpv4Range(Ipv4Range ipv4Range) {
    this.ipv4Range = ipv4Range;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  @Override
  public String toString() {
    return "InetnumLocation{"
        + "name='"
        + name
        + '\''
        + ", ipv4Range="
        + ipv4Range
        + ", location="
        + location
        + '}';
  }
}
