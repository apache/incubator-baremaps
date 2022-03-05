/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.osm;

import static com.baremaps.testing.TestFiles.DATA_OSC_XML;
import static com.baremaps.testing.TestFiles.DATA_OSM_PBF;
import static com.baremaps.testing.TestFiles.DATA_OSM_XML;
import static com.baremaps.testing.TestFiles.DENSE_NODES_OSM_PBF;
import static com.baremaps.testing.TestFiles.MONACO_OSM_BZ2;
import static com.baremaps.testing.TestFiles.MONACO_OSM_PBF;
import static com.baremaps.testing.TestFiles.MONACO_STATE_TXT;
import static com.baremaps.testing.TestFiles.RELATIONS_OSM_PBF;
import static com.baremaps.testing.TestFiles.WAYS_OSM_PBF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.baremaps.osm.change.OsmChangeParser;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.State;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.function.EntityConsumer;
import com.baremaps.osm.pbf.OsmPbfParser;
import com.baremaps.osm.state.OsmStateParser;
import com.baremaps.osm.xml.OsmXmlParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.junit.jupiter.api.Test;

class OpenStreetMapTest {

  @Test
  void dataOsmXml() throws IOException {
    try (InputStream input = DATA_OSM_XML.openStream()) {
      assertEquals(12, new OsmXmlParser().entities(input).collect(Collectors.toList()).size());
    }
  }

  @Test
  void dataOsmXmlNodes() throws IOException {
    try (InputStream input = DATA_OSM_XML.openStream()) {
      assertEquals(6, new OsmXmlParser().entities(input).filter(e -> e instanceof Node).count());
    }
  }

  @Test
  void dataOsmXmlWays() throws IOException {
    try (InputStream input = DATA_OSM_XML.openStream()) {
      assertEquals(3, new OsmXmlParser().entities(input).filter(e -> e instanceof Way).count());
    }
  }

  @Test
  void dataOsmXmlRelations() throws IOException {
    try (InputStream input = DATA_OSM_XML.openStream()) {
      assertEquals(
          1, new OsmXmlParser().entities(input).filter(e -> e instanceof Relation).count());
    }
  }

  @Test
  void dataOscXml() throws IOException {
    try (InputStream input = DATA_OSC_XML.openStream()) {
      assertEquals(7, new OsmChangeParser().changes(input).collect(Collectors.toList()).size());
    }
  }

  @Test
  void dataOsmPbf() throws IOException {
    try (InputStream input = DATA_OSM_PBF.openStream()) {
      assertEquals(72002, new OsmPbfParser().entities(input).count());
    }
  }

  @Test
  void denseNodesOsmPbf() throws IOException {
    try (InputStream input = DENSE_NODES_OSM_PBF.openStream()) {
      assertEquals(8000, new OsmPbfParser().entities(input).filter(e -> e instanceof Node).count());
    }
  }

  @Test
  void waysOsmPbf() throws IOException {
    try (InputStream input = WAYS_OSM_PBF.openStream()) {
      assertEquals(8000, new OsmPbfParser().entities(input).filter(e -> e instanceof Way).count());
    }
  }

  @Test
  void relationsOsmPbf() throws IOException {
    try (InputStream input = RELATIONS_OSM_PBF.openStream()) {
      assertEquals(
          8000, new OsmPbfParser().entities(input).filter(e -> e instanceof Relation).count());
    }
  }

  @Test
  void monacoStateTxt() throws URISyntaxException, IOException {
    try (InputStream inputStream = MONACO_STATE_TXT.openStream()) {
      State state = new OsmStateParser().state(inputStream);
      assertEquals(2788, state.getSequenceNumber());
      assertEquals(LocalDateTime.parse("2020-11-10T21:42:03"), state.getTimestamp());
    }
  }

  @Test
  void monacoOsmPbf() throws IOException, URISyntaxException {
    try (InputStream inputStream = MONACO_OSM_PBF.openStream()) {
      Stream<Entity> stream = new OsmPbfParser().entities(inputStream);
      process(stream, 1, 1, 25002, 4018, 243);
    }
  }

  @Test
  void monacoOsmBz2() throws IOException, URISyntaxException {
    try (InputStream inputStream = new BZip2CompressorInputStream(MONACO_OSM_BZ2.openStream())) {
      Stream<Entity> stream = new OsmXmlParser().entities(inputStream);
      process(stream, 1, 1, 24951, 4015, 243);
    }
  }

  void process(
      Stream<Entity> stream,
      long headerCount,
      long boundCount,
      long nodeCount,
      long wayCount,
      long relationCount) {
    AtomicLong headers = new AtomicLong(0);
    AtomicLong bounds = new AtomicLong(0);
    AtomicLong nodes = new AtomicLong(0);
    AtomicLong ways = new AtomicLong(0);
    AtomicLong relations = new AtomicLong(0);
    stream.forEach(
        new EntityConsumer() {
          @Override
          public void match(Header header) {
            assertNotNull(header);
            assertEquals("osmium/1.8.0", header.getWritingProgram());
            headers.incrementAndGet();
          }

          @Override
          public void match(Bound bound) {
            assertNotNull(bound);
            assertEquals(43.75169, bound.getMaxLat(), 0.000001);
            assertEquals(7.448637, bound.getMaxLon(), 0.000001);
            assertEquals(43.72335, bound.getMinLat(), 0.000001);
            assertEquals(7.409205, bound.getMinLon(), 0.000001);
            bounds.incrementAndGet();
          }

          @Override
          public void match(Node node) {
            assertNotNull(node);
            nodes.incrementAndGet();
          }

          @Override
          public void match(Way way) {
            assertNotNull(way);
            ways.incrementAndGet();
          }

          @Override
          public void match(Relation relation) {
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
