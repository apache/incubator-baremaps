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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.State;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.handler.EntityHandler;
import com.baremaps.osm.state.StateReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.junit.jupiter.api.Test;

public class OpenStreetMapTest {

  @Test
  void dataOsmXml() throws IOException {
    try (InputStream input = DATA_OSM_XML.openStream()) {
      assertEquals(12,
          OpenStreetMap.streamXmlEntities(input)
              .collect(Collectors.toList()).size());
    }
  }

  @Test
  void dataOsmXmlNodes() throws IOException {
    try (InputStream input = DATA_OSM_XML.openStream()) {
      assertEquals(6,
          OpenStreetMap.streamXmlEntities(input)
              .filter(e -> e instanceof Node)
              .count());
    }
  }

  @Test
  void dataOsmXmlWays() throws IOException {
    try (InputStream input = DATA_OSM_XML.openStream()) {
      assertEquals(3,
          OpenStreetMap.streamXmlEntities(input)
              .filter(e -> e instanceof Way)
              .count());
    }
  }

  @Test
  void dataOsmXmlRelations() throws IOException {
    try (InputStream input = DATA_OSM_XML.openStream()) {
      assertEquals(1,
          OpenStreetMap.streamXmlEntities(input)
              .filter(e -> e instanceof Relation)
              .count());
    }
  }

  @Test
  void dataOscXml() throws IOException {
    try (InputStream input = DATA_OSC_XML.openStream()) {
      assertEquals(7,
          OpenStreetMap.streamXmlChanges(input)
              .collect(Collectors.toList())
              .size());
    }
  }

  @Test
  void dataOsmPbf() throws IOException {
    try (InputStream input = DATA_OSM_PBF.openStream()) {
      assertEquals(72002,
          OpenStreetMap.streamPbfEntities(input)
              .count());
    }

  }

  @Test
  void denseNodesOsmPbf() throws IOException {
    try (InputStream input = DENSE_NODES_OSM_PBF.openStream()) {
      assertEquals(8000,
          OpenStreetMap.streamPbfEntities(input)
              .filter(e -> e instanceof Node)
              .count());
    }

  }

  @Test
  void waysOsmPbf() throws IOException {
    try (InputStream input = WAYS_OSM_PBF.openStream()) {
      assertEquals(8000,
          OpenStreetMap.streamPbfEntities(input)
              .filter(e -> e instanceof Way)
              .count());
    }
  }

  @Test
  void relationsOsmPbf() throws IOException {
    try (InputStream input = RELATIONS_OSM_PBF.openStream()) {
      assertEquals(8000,
          OpenStreetMap.streamPbfEntities(input)
              .filter(e -> e instanceof Relation)
              .count());
    }
  }

  @Test
  void monacoStateTxt() throws URISyntaxException, IOException {
    try (InputStream inputStream = MONACO_STATE_TXT.openStream()) {
      State state = new StateReader(inputStream).read();
      assertEquals(2788, state.getSequenceNumber());
      assertEquals(LocalDateTime.parse("2020-11-10T21:42:03"), state.getTimestamp());
    }
  }

  @Test
  void monacoOsmPbf() throws IOException, URISyntaxException {
    try (InputStream inputStream = MONACO_OSM_PBF.openStream()) {
      Stream<Entity> stream = OpenStreetMap.streamPbfEntities(inputStream);
      process(stream, 1, 1, 25002, 4018, 243);
    }
  }

  @Test
  void monacoOsmBz2() throws IOException, URISyntaxException {
    try (InputStream inputStream = new BZip2CompressorInputStream(MONACO_OSM_BZ2.openStream())) {
      Stream<Entity> stream = OpenStreetMap.streamXmlEntities(inputStream);
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
    stream.forEach(new EntityHandler() {
      @Override
      public void handle(Header header) {
        assertTrue(header != null);
        assertEquals("osmium/1.8.0", header.getWritingProgram());
        headers.incrementAndGet();
      }

      @Override
      public void handle(Bound bound) {
        assertTrue(bound != null);
        assertEquals(43.75169, bound.getMaxLat(), 0.000001);
        assertEquals(7.448637, bound.getMaxLon(), 0.000001);
        assertEquals(43.72335, bound.getMinLat(), 0.000001);
        assertEquals(7.409205, bound.getMinLon(), 0.000001);
        bounds.incrementAndGet();
      }

      @Override
      public void handle(Node node) {
        assertTrue(node != null);
        nodes.incrementAndGet();
      }

      @Override
      public void handle(Way way) {
        assertTrue(way != null);
        ways.incrementAndGet();
      }

      @Override
      public void handle(Relation relation) {
        assertTrue(relation != null);
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
