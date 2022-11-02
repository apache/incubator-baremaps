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

package org.apache.baremaps.iploc.data;

/** Contains a location comprising a latitude and a longitude */
public class Location {

  private double latitude;
  private double longitude;

  /**
   * Create a new location using the given latitude and longitude
   *
   * @param latitude
   * @param longitude
   */
  public Location(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  /**
   * Returns the latitude
   *
   * @return the latitude
   */
  public double getLatitude() {
    return latitude;
  }

  /**
   * Returns the longitude
   *
   * @return the longitude
   */
  public double getLongitude() {
    return longitude;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "Location{" + "latitude=" + latitude + ", longitude=" + longitude + '}';
  }
}
