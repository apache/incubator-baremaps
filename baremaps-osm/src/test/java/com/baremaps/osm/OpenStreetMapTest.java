package com.baremaps.osm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

public class OpenStreetMapTest {

  @Test
  void monacoOsmPbf() throws IOException, URISyntaxException {
    parse(TestFiles.monacoOsmPbf());
  }

  @Test
  void monacoOsmBz2() throws IOException, URISyntaxException {
    parse(TestFiles.monacoOsmBz2());
  }

  void parse(Path path) throws IOException {
    AtomicLong headers = new AtomicLong(0);
    AtomicLong bounds = new AtomicLong(0);
    AtomicLong nodes = new AtomicLong(0);
    AtomicLong ways = new AtomicLong(0);
    AtomicLong relations = new AtomicLong(0);
    OpenStreetMap.entityStream(path).forEach(new EntityHandler() {
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
    assertEquals(1, headers.get());
    assertEquals(1, bounds.get());
    assertTrue(24000 < nodes.get());
    assertTrue(4000 < ways.get());
    assertTrue(240 < relations.get());
  }

}
