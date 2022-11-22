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

package org.apache.baremaps.openstreetmap;

import static org.apache.baremaps.testing.TestFiles.DATA_OSC_XML;
import static org.apache.baremaps.testing.TestFiles.DATA_OSM_PBF;
import static org.apache.baremaps.testing.TestFiles.DATA_OSM_XML;
import static org.apache.baremaps.testing.TestFiles.DENSE_NODES_OSM_PBF;
import static org.apache.baremaps.testing.TestFiles.MONACO_OSM_BZ2;
import static org.apache.baremaps.testing.TestFiles.MONACO_OSM_PBF;
import static org.apache.baremaps.testing.TestFiles.MONACO_STATE_TXT;
import static org.apache.baremaps.testing.TestFiles.RELATIONS_OSM_PBF;
import static org.apache.baremaps.testing.TestFiles.WAYS_OSM_PBF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.baremaps.openstreetmap.model.Bound;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.openstreetmap.model.Header;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.State;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.openstreetmap.pbf.PbfBlockReader;
import org.apache.baremaps.openstreetmap.pbf.PbfEntityReader;
import org.apache.baremaps.openstreetmap.state.StateReader;
import org.apache.baremaps.openstreetmap.xml.XmlChangeReader;
import org.apache.baremaps.openstreetmap.xml.XmlEntityReader;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.junit.jupiter.api.Test;

class OpenStreetMapTest {

  @Test
  void dataOsmXml() throws IOException {
    try (InputStream input = Files.newInputStream(DATA_OSM_XML)) {
      assertEquals(12, new XmlEntityReader().stream(input).collect(Collectors.toList()).size());
    }
  }

  @Test
  void dataOsmXmlNodes() throws IOException {
    try (InputStream input = Files.newInputStream(DATA_OSM_XML)) {
      assertEquals(6, new XmlEntityReader().stream(input).filter(e -> e instanceof Node).count());
    }
  }

  @Test
  void dataOsmXmlWays() throws IOException {
    try (InputStream input = Files.newInputStream(DATA_OSM_XML)) {
      assertEquals(3, new XmlEntityReader().stream(input).filter(e -> e instanceof Way).count());
    }
  }

  @Test
  void dataOsmXmlRelations() throws IOException {
    try (InputStream input = Files.newInputStream(DATA_OSM_XML)) {
      assertEquals(1,
          new XmlEntityReader().stream(input).filter(e -> e instanceof Relation).count());
    }
  }

  @Test
  void dataOscXml() throws IOException {
    try (InputStream input = Files.newInputStream(DATA_OSC_XML)) {
      assertEquals(7, new XmlChangeReader().stream(input).collect(Collectors.toList()).size());
    }
  }

  @Test
  void dataOsmPbf() throws IOException {
    try (InputStream input = Files.newInputStream(DATA_OSM_PBF)) {
      assertEquals(72002, new PbfEntityReader(new PbfBlockReader()).stream(input).count());
    }
  }

  @Test
  void denseNodesOsmPbf() throws IOException {
    try (InputStream input = Files.newInputStream(DENSE_NODES_OSM_PBF)) {
      assertEquals(8000, new PbfEntityReader(new PbfBlockReader()).stream(input)
          .filter(e -> e instanceof Node).count());
    }
  }

  @Test
  void waysOsmPbf() throws IOException {
    try (InputStream input = Files.newInputStream(WAYS_OSM_PBF)) {
      assertEquals(8000, new PbfEntityReader(new PbfBlockReader()).stream(input)
          .filter(e -> e instanceof Way).count());
    }
  }

  @Test
  void relationsOsmPbf() throws IOException {
    try (InputStream input = Files.newInputStream(RELATIONS_OSM_PBF)) {
      assertEquals(8000, new PbfEntityReader(new PbfBlockReader()).stream(input)
          .filter(e -> e instanceof Relation).count());
    }
  }

  @Test
  void monacoStateTxt() throws URISyntaxException, IOException {
    try (InputStream inputStream = Files.newInputStream(MONACO_STATE_TXT)) {
      State state = new StateReader().state(inputStream);
      assertEquals(2788, state.getSequenceNumber());
      assertEquals(LocalDateTime.parse("2020-11-10T21:42:03"), state.getTimestamp());
    }
  }

  @Test
  void monacoOsmPbf() throws IOException, URISyntaxException {
    try (InputStream inputStream = Files.newInputStream(MONACO_OSM_PBF)) {
      Stream<Entity> stream = new PbfEntityReader(new PbfBlockReader()).stream(inputStream);
      process(stream, 1, 1, 25002, 4018, 243);
    }
  }

  @Test
  void monacoOsmBz2() throws IOException, URISyntaxException {
    try (InputStream inputStream =
        new BZip2CompressorInputStream(Files.newInputStream(MONACO_OSM_BZ2))) {
      Stream<Entity> stream = new XmlEntityReader().stream(inputStream);
      process(stream, 1, 1, 24951, 4015, 243);
    }
  }

  void process(Stream<Entity> stream, long headerCount, long boundCount, long nodeCount,
      long wayCount, long relationCount) {
    AtomicLong headers = new AtomicLong(0);
    AtomicLong bounds = new AtomicLong(0);
    AtomicLong nodes = new AtomicLong(0);
    AtomicLong ways = new AtomicLong(0);
    AtomicLong relations = new AtomicLong(0);
    stream.forEach(entity -> {
      if (entity instanceof Header header) {
        assertNotNull(header);
        assertEquals("osmium/1.8.0", header.getWritingProgram());
        headers.incrementAndGet();
      } else if (entity instanceof Bound bound) {
        assertNotNull(bound);
        assertEquals(43.75169, bound.getMaxLat(), 0.000001);
        assertEquals(7.448637, bound.getMaxLon(), 0.000001);
        assertEquals(43.72335, bound.getMinLat(), 0.000001);
        assertEquals(7.409205, bound.getMinLon(), 0.000001);
        bounds.incrementAndGet();
      } else if (entity instanceof Node node) {
        assertNotNull(node);
        nodes.incrementAndGet();
      } else if (entity instanceof Way way) {
        assertNotNull(way);
        ways.incrementAndGet();
      } else if (entity instanceof Relation relation) {
        assertNotNull(relation);
        relations.incrementAndGet();
      }
    });
    assertEquals(headerCount, headers.get());
    assertEquals(boundCount, bounds.get());
    assertEquals(nodeCount, nodes.get());
    assertEquals(wayCount, ways.get());
    assertEquals(relationCount, relations.get());
  }

  class Stats {

    public long counter = 0;
    public long max = 0;
  }
}
