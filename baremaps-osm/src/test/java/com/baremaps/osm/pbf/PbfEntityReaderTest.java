package com.baremaps.osm.pbf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.osm.TestFiles;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class PbfEntityReaderTest {

  @Test
  void entities() throws IOException {
    assertEquals(72002, new PbfEntityReader(TestFiles.dataOsmPbf())
        .stream()
        .count());
  }

  @Test
  void nodes() throws IOException {
    assertEquals(8000, new PbfEntityReader(TestFiles.denseNodesOsmPbf())
        .stream()
        .filter(e -> e instanceof Node)
        .count());
  }

  @Test
  void ways() throws IOException {
    assertEquals(8000, new PbfEntityReader(TestFiles.waysOsmPbf())
        .stream()
        .filter(e -> e instanceof Way)
        .count());
  }

  @Test
  void relations() throws IOException {
    assertEquals(8000, new PbfEntityReader(TestFiles.relationsOsmPbf())
        .stream()
        .filter(e -> e instanceof Relation)
        .count());
  }



}