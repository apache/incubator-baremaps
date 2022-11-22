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

package org.apache.baremaps.openstreetmap.model;



import java.util.Objects;
import java.util.StringJoiner;

/** Represents the bounds of an OpenStreetMap dataset. */
public final class Bound implements Entity {

  private final double maxLat;

  private final double maxLon;

  private final double minLat;

  private final double minLon;

  /**
   * Consturcts a {@code Bound} with the specified limits.
   *
   * @param maxLat the max latitude
   * @param maxLon the max longitude
   * @param minLat the min latitude
   * @param minLon the max longitude
   */
  public Bound(double maxLat, double maxLon, double minLat, double minLon) {
    this.maxLat = maxLat;
    this.maxLon = maxLon;
    this.minLat = minLat;
    this.minLon = minLon;
  }

  /**
   * Returns the max latitude.
   *
   * @return the max latitude
   */
  public double getMaxLat() {
    return maxLat;
  }

  /**
   * Returns the max longitude.
   *
   * @return the max longitude
   */
  public double getMaxLon() {
    return maxLon;
  }

  /**
   * Returns the min latitude.
   *
   * @return the min latitude
   */
  public double getMinLat() {
    return minLat;
  }

  /**
   * Returns the min longitude.
   *
   * @return the min longitude
   */
  public double getMinLon() {
    return minLon;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Bound)) {
      return false;
    }
    Bound bound = (Bound) o;
    return Double.compare(bound.maxLat, maxLat) == 0 && Double.compare(bound.maxLon, maxLon) == 0
        && Double.compare(bound.minLat, minLat) == 0 && Double.compare(bound.minLon, minLon) == 0;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(maxLat, maxLon, minLat, minLon);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return new StringJoiner(", ", Bound.class.getSimpleName() + "[", "]").add("maxLat=" + maxLat)
        .add("maxLon=" + maxLon).add("minLat=" + minLat).add("minLon=" + minLon).toString();
  }
}
