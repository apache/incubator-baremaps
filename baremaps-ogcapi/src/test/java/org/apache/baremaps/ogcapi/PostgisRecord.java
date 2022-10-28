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

package org.apache.baremaps.ogcapi;



import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class PostgisRecord {

  private Integer id;
  private Point point;
  private LineString lineString;
  private Polygon polygon;

  public PostgisRecord() {}

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Point getPoint() {
    return point;
  }

  public void setPoint(Point point) {
    this.point = point;
  }

  public LineString getLineString() {
    return lineString;
  }

  public void setLineString(LineString lineString) {
    this.lineString = lineString;
  }

  public Polygon getPolygon() {
    return polygon;
  }

  public void setPolygon(Polygon polygon) {
    this.polygon = polygon;
  }
}
