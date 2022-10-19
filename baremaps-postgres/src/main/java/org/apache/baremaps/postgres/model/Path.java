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



import java.util.List;

public class Path {

  private final boolean isClosed;
  private final List<Point> points;

  public Path(boolean closed, List<Point> points) {

    if (points == null) {
      throw new IllegalArgumentException("points");
    }

    this.isClosed = closed;
    this.points = points;
  }

  public boolean isClosed() {
    return isClosed;
  }

  public List<Point> getPoints() {
    return points;
  }

  public int size() {
    return points.size();
  }
}
