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

package org.apache.baremaps.openstreetmap.function;



import java.util.function.Function;
import org.apache.baremaps.openstreetmap.model.Element;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LatLonShape;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.geo.Line;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Maps an OpenStreetMap element to a Lucene document.
 */
public class OpenstreetmapDocumentMapper implements Function<Element, Document> {

  @Override
  public Document apply(Element element) {
    var document = new Document();

    document.add(new StoredField("id", element.getId()));

    element.getTags().forEach((key, value) -> {
      document.add(new StoredField(key, value));
    });

    var geometry = element.getGeometry();
    if (geometry instanceof Point point) {
      document.add(LatLonShape.createDocValueField("geometry", point.getY(), point.getX()));
    } else if (geometry instanceof LineString lineString) {
      var coordinates = lineString.getCoordinates();
      var lats = new double[coordinates.length];
      var lons = new double[coordinates.length];
      for (int i = 0; i < coordinates.length; i++) {
        lats[i] = coordinates[i].y;
        lons[i] = coordinates[i].x;
      }
      document.add(LatLonShape.createDocValueField("geometry", new Line(lats, lons)));
    } else if (geometry instanceof Polygon polygon) {
      var coordinates = polygon.getCoordinates();
      var lats = new double[coordinates.length];
      var lons = new double[coordinates.length];
      for (int i = 0; i < coordinates.length; i++) {
        lats[i] = coordinates[i].y;
        lons[i] = coordinates[i].x;
      }
      document.add(LatLonShape.createDocValueField("geometry",
          new org.apache.lucene.geo.Polygon(lats, lons)));
    } else if (geometry instanceof MultiPolygon) {
      // TODO: Implement MultiPolygon
    }

    return document;
  }
}
