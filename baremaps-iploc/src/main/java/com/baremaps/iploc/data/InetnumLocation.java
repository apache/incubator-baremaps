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
  private String address;
  private Ipv4Range ipv4Range;
  private Location location;
  private String network;
  private String country;

  /**
   * Create new Inetnum location
   *
   * @param name
   * @param ipv4Range
   * @param location
   */
  public InetnumLocation(
      String name, Ipv4Range ipv4Range, Location location, String network, String country) {
    this.address = name;
    this.ipv4Range = ipv4Range;
    this.location = location;
    this.network = network;
    this.country = country;
  }

  /**
   * Returns the name
   *
   * @return the name
   */
  public String getAddress() {
    return address;
  }

  /**
   * Returns the ipv4 range
   *
   * @return the ipv4 range
   */
  public Ipv4Range getIpv4Range() {
    return ipv4Range;
  }

  /**
   * Returns the location
   *
   * @return the location
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Returns the network
   *
   * @return the network
   */
  public String getNetwork() {
    return network;
  }

  /**
   * Returns the country
   *
   * @return the country
   */
  public String getCountry() {
    return country;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "InetnumLocation{"
        + "name='"
        + address
        + ", ipv4Range="
        + ipv4Range
        + ", location="
        + location
        + ", network="
        + network
        + "}";
  }
}
