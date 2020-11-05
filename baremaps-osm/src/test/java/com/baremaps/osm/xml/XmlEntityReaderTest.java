package com.baremaps.osm.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.osm.DataFiles;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class XmlEntityReaderTest {

  @Test
  void entities() throws IOException {
    assertEquals(12, new XmlEntityReader(DataFiles.dataOsmXml())
        .read()
        .count());
    assertEquals(6, new XmlEntityReader(DataFiles.dataOsmXml())
        .read()
        .filter(e -> e instanceof Node)
        .count());
    assertEquals(3, new XmlEntityReader(DataFiles.dataOsmXml())
        .read()
        .filter(e -> e instanceof Way)
        .count());
    assertEquals(1, new XmlEntityReader(DataFiles.dataOsmXml())
        .read()
        .filter(e -> e instanceof Relation)
        .count());
  }

}