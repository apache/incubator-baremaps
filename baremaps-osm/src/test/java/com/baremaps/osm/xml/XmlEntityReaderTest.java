package com.baremaps.osm.xml;

import static org.junit.jupiter.api.Assertions.*;

import com.baremaps.osm.DataFiles;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.pbf.PbfEntityReader;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class XmlEntityReaderTest {

  @Test
  void entities() throws IOException {
    assertEquals(12, new XmlEntityReader(DataFiles.dataOsmXml())
        .entities()
        .count());
    assertEquals(6, new XmlEntityReader(DataFiles.dataOsmXml())
        .entities()
        .filter(e -> e instanceof Node)
        .count());
    assertEquals(3, new XmlEntityReader(DataFiles.dataOsmXml())
        .entities()
        .filter(e -> e instanceof Way)
        .count());
    assertEquals(1, new XmlEntityReader(DataFiles.dataOsmXml())
        .entities()
        .filter(e -> e instanceof Relation)
        .count());
  }

}