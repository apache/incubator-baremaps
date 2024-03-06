/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.openstreetmap;

import static org.apache.baremaps.testing.OsmSample.SAMPLE_OSM_PBF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.apache.baremaps.openstreetmap.model.Bound;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.openstreetmap.model.Header;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.State;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.openstreetmap.pbf.PbfEntityReader;
import org.apache.baremaps.openstreetmap.state.StateReader;
import org.apache.baremaps.openstreetmap.xml.XmlEntityReader;
import org.apache.baremaps.testing.OsmSample;
import org.junit.jupiter.api.Test;

class OsmSampleTest {

  @Test
  void sampleStateTxt() throws IOException {
    try (InputStream inputStream = Files.newInputStream(OsmSample.SAMPLE_STATE_TXT)) {
      State state = new StateReader().readState(inputStream);
      assertEquals(1, state.getSequenceNumber());
      assertEquals(LocalDateTime.parse("2000-01-01T00:00:00"), state.getTimestamp());
    }
  }

  @Test
  void sampleOsmPbf() throws IOException {
    try (InputStream inputStream = Files.newInputStream(SAMPLE_OSM_PBF)) {
      Stream<Entity> stream = new PbfEntityReader().stream(inputStream);
      process(stream, 1, 1, 27, 7, 2);
    }
  }

  @Test
  void sampleOsmXml() throws IOException {
    try (InputStream inputStream = Files.newInputStream(OsmSample.SAMPLE_OSM_XML)) {
      Stream<Entity> stream = new XmlEntityReader().stream(inputStream);
      process(stream, 1, 1, 27, 7, 2);
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
        assertEquals("osmium/1.16.0", header.getWritingProgram());
        headers.incrementAndGet();
      } else if (entity instanceof Bound bound) {
        assertNotNull(bound);
        assertEquals(0.0, bound.getMinLat(), 0.000001);
        assertEquals(0.0, bound.getMinLon(), 0.000001);
        assertEquals(20.0, bound.getMaxLat(), 0.000001);
        assertEquals(20.0, bound.getMaxLon(), 0.000001);
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
}
