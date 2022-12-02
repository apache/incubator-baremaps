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

package org.apache.baremaps.openstreetmap.geometry;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.openstreetmap.function.RelationGeometryBuilder;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.openstreetmap.store.MockLongDataMap;
import org.apache.baremaps.openstreetmap.xml.XmlEntityReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

class RelationGeometryBuilderTest {

  Geometry handleRelation(String file) throws IOException {
    InputStream input = new GZIPInputStream(this.getClass().getResourceAsStream(file));
    List<Entity> entities = new XmlEntityReader().stream(input).toList();
    LongDataMap<Coordinate> coordinateMap = new MockLongDataMap<>(
        entities.stream().filter(e -> e instanceof Node).map(e -> (Node) e).collect(
            Collectors.toMap(n -> n.getId(), n -> new Coordinate(n.getLon(), n.getLat()))));
    LongDataMap<List<Long>> referenceMap =
        new MockLongDataMap<>(entities.stream().filter(e -> e instanceof Way).map(e -> (Way) e)
            .collect(Collectors.toMap(w -> w.getId(), w -> w.getNodes())));
    Relation relation = entities.stream().filter(e -> e instanceof Relation).map(e -> (Relation) e)
        .findFirst().get();
    new RelationGeometryBuilder(coordinateMap, referenceMap).accept(relation);
    return relation.getGeometry();
  }

  @Test
  void handleRelation381076() throws IOException {
    assertNotNull(handleRelation("/complex/381076.osm.gz"));
  }

  @Test
  void handleRelation1450537() throws IOException {
    assertNotNull(handleRelation("/complex/1450537.osm.gz"));
  }

  @Test
  void handleRelation1436294() throws IOException {
    assertNotNull(handleRelation("/complex/1436294.osm.gz"));
  }

  // The following relations generate exceptions in Planet OSM.

  @Test
  @Disabled
  void handleRelation1374114() throws IOException {
    assertNotNull(handleRelation("/complex/1374114.osm.gz"));
  }

  @Test
  @Disabled
  void handleRelation3224199() throws IOException {
    assertNotNull(handleRelation("/complex/3224199.osm.gz"));
  }

  @Test
  @Disabled
  void handleRelation3492638() throws IOException {
    assertNotNull(handleRelation("/complex/3492638.osm.gz"));
  }

  @Test
  @Disabled
  void handleRelation550505() throws IOException {
    assertNotNull(handleRelation("/complex/550505.osm.gz"));
  }

  @Test
  @Disabled
  void handleRelatio8142542() throws IOException {
    assertNotNull(handleRelation("/complex/8142542.osm.gz"));
  }

  @Test
  @Disabled
  void handleRelation8165292() throws IOException {
    assertNotNull(handleRelation("/complex/8165292.osm.gz"));
  }
}
