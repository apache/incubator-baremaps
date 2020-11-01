package com.baremaps.osm.pbf;

import static org.junit.jupiter.api.Assertions.*;

import com.baremaps.osm.DataFiles;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Way;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class PbfEntityReaderTest {

  @Test
  void entities() throws IOException {
    assertEquals(72002, new PbfEntityReader(DataFiles.dataOsmPbf()).entities().count());
    assertEquals(8000, new PbfEntityReader(DataFiles.denseNodesOsmPbf()).entities().count());
    assertEquals(8000, new PbfEntityReader(DataFiles.waysOsmPbf()).entities().count());
    assertEquals(8000, new PbfEntityReader(DataFiles.relationsOsmPbf()).entities().count());
  }
}