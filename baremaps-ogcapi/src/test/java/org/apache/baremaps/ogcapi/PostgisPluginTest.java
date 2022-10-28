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



import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

class PostgisPluginTest {

  @Test
  @Tag("integration")
  void test() {
    Jdbi jdbi = Jdbi.create("jdbc:tc:postgis:13-3.1:///test").installPlugin(new PostgisPlugin());

    PostgisRecord record = new PostgisRecord();
    record.setId(1);
    record.setPoint(new GeometryFactory().createPoint(new Coordinate(1, 1)));
    record.setLineString(new GeometryFactory().createLineString(new Coordinate[] {
        new Coordinate(1, 1), new Coordinate(1, 2), new Coordinate(2, 2), new Coordinate(2, 1)}));
    record.setPolygon(new GeometryFactory().createPolygon(new Coordinate[] {new Coordinate(1, 1),
        new Coordinate(1, 2), new Coordinate(2, 2), new Coordinate(2, 1), new Coordinate(1, 1),}));

    List<PostgisRecord> result = jdbi.withHandle(handle -> {
      handle.execute("DROP TABLE IF EXISTS record");
      handle.execute(
          "CREATE TABLE record (id INTEGER PRIMARY KEY, point geometry(point), linestring geometry(linestring), polygon geometry(polygon))");
      handle.createUpdate(
          "INSERT INTO record (id, point, linestring, polygon) VALUES (:id, :point, :lineString, :polygon)")
          .bindBean(record).execute();
      return handle.createQuery("SELECT * FROM record ORDER BY id").mapToBean(PostgisRecord.class)
          .list();
    });

    Assertions.assertEquals(record.getPoint(), result.get(0).getPoint());
    Assertions.assertEquals(record.getLineString(), result.get(0).getLineString());
    Assertions.assertEquals(record.getPolygon(), result.get(0).getPolygon());
  }
}
