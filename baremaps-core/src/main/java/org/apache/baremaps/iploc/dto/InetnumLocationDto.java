/*
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

package org.apache.baremaps.iploc.dto;



import org.apache.baremaps.iploc.data.InetnumLocation;
import org.apache.baremaps.iploc.data.Ipv4;

public class InetnumLocationDto {

  private final String address;

  private final String ipv4Start;

  private final String ipv4End;

  private final double latitude;

  private final double longitude;

  private final String network;

  private final String country;

  public InetnumLocationDto(InetnumLocation inetnumLocation) {
    this.address = inetnumLocation.getAddress();
    this.ipv4Start = Ipv4.format(inetnumLocation.getIpv4Range().getStart());
    this.ipv4End = Ipv4.format(inetnumLocation.getIpv4Range().getEnd());
    this.latitude = inetnumLocation.getLocation().getLatitude();
    this.longitude = inetnumLocation.getLocation().getLongitude();
    this.network = inetnumLocation.getNetwork();
    this.country = inetnumLocation.getCountry();
  }

  public String getAddress() {
    return address;
  }

  public String getIpv4Start() {
    return ipv4Start;
  }

  public String getIpv4End() {
    return ipv4End;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public String getNetwork() {
    return network;
  }

  public String getCountry() {
    return country;
  }

  @Override
  public String toString() {
    return "InetnumLocationDto{" + "address='" + address + '\'' + ", ipv4Start='" + ipv4Start + '\''
        + ", ipv4End='" + ipv4End + '\'' + ", latitude=" + latitude + ", longitude=" + longitude
        + ", network='" + network + '\'' + ", country='" + country + '\'' + '}';
  }
}
