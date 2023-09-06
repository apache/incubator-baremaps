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

package org.apache.baremaps.geocoderosm;



import java.text.ParseException;
import java.util.function.Function;
import org.apache.baremaps.openstreetmap.model.Element;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LatLonShape;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.geo.Polygon;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;


public class OSMNodeDocumentMapper implements Function<Element, Document> {

  @Override
  public Document apply(Element element) {
    Document document = new Document();
    if (element.getTags().containsKey(OSMTags.NAME.key())) {
      document.add(
          new TextField(OSMTags.NAME.key(), element.getTags().get(OSMTags.NAME.key()).toString(),
              Field.Store.YES));
    }

    if (element instanceof Node node) {
      document.add(LatLonShape.createIndexableFields("polygon", node.getLat(), node.getLon())[0]);
      document.add(new StoredField("latitude", node.getLat()));
      document.add(new StoredField("longitude", node.getLon()));
    }
    if (element.getGeometry() != null
        && !element.getGeometry().getGeometryType().equals(Geometry.TYPENAME_POINT)) {
      // JTS to GeoJSON
      var geojsonWriter = new GeoJsonWriter();
      // Remove crs field in GeoJSON as Lucene parsing is very strict.
      // Avoid "crs must be CRS84 from OGC, but saw: EPSG:4326"
      // See:
      // https://github.com/apache/lucene/blob/ef42af65f27f7f078b1ab426de9f2b2fa214ad86/lucene/core/src/java/org/apache/lucene/geo/SimpleGeoJSONPolygonParser.java#L180
      geojsonWriter.setEncodeCRS(false);
      // Assume that Geometry is in EPSG:4326/WGS84 for Lucene Polygon.fromGeoJSON
      var geojson = geojsonWriter.write(element.getGeometry());

      // GeoJSON to Lucene Polygon
      try {
        var polygons = Polygon.fromGeoJSON(geojson);

        for (Polygon polygon : polygons) {
          // LatLonShape.createIndexableFields can create multiple polygons out of a single polygon
          // through tesselation
          for (Field field : LatLonShape.createIndexableFields("polygon", polygon)) {
            document.add(field);
          }
        }
      } catch (ParseException e) {
        // ignore geometry
        System.out.println("geometry failed: " + e);
      }

    }



    // document.add(new StoredField(OSMTags.LATITUDE.key(), node.getLat()));
    // document.add(new StoredField(OSMTags.LONGITUDE.key(), node.getLon()));
    if (element.getTags().containsKey(OSMTags.POPULATION.key())) {
      var population = Long.parseLong(element.getTags().get(OSMTags.POPULATION.key()).toString());
      document.add(new NumericDocValuesField(OSMTags.POPULATION.key(), population));
      document.add(new StoredField(OSMTags.POPULATION.key(), population));
    }
    return document;
  }

}
