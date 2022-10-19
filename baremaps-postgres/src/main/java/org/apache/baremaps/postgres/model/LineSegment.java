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

package org.apache.baremaps.postgres.model;

public class LineSegment {

  private final Point p1;
  private final Point p2;

  public LineSegment(Point p1, Point p2) {

    if (p1 == null) {
      throw new IllegalArgumentException("p1");
    }

    if (p2 == null) {
      throw new IllegalArgumentException("p2");
    }

    this.p1 = p1;
    this.p2 = p2;
  }

  public Point getP1() {
    return p1;
  }

  public Point getP2() {
    return p2;
  }
}
