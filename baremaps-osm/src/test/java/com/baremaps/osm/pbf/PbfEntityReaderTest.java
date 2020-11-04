package com.baremaps.osm.pbf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.osm.DataFiles;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class PbfEntityReaderTest {

  @Test
  void entities() throws IOException {
    assertEquals(72002, new PbfEntityReader(DataFiles.dataOsmPbf())
        .read()
        .count());
    assertEquals(8000, new PbfEntityReader(DataFiles.denseNodesOsmPbf())
        .read()
        .filter(e -> e instanceof Node)
        .count());
    assertEquals(8000, new PbfEntityReader(DataFiles.waysOsmPbf())
        .read()
        .filter(e -> e instanceof Way)
        .count());
    assertEquals(8000, new PbfEntityReader(DataFiles.relationsOsmPbf())
        .read()
        .filter(e -> e instanceof Relation)
        .count());
  }
}