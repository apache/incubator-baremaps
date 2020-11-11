package com.baremaps.osm.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.osm.TestFiles;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class XmlEntityReaderTest {

  @Test
  void entities() throws IOException {
    assertEquals(12,
        new XmlEntityReader(TestFiles.dataOsmXml())
            .read()
            .count());
    assertEquals(6,
        new XmlEntityReader(TestFiles.dataOsmXml())
            .read()
            .filter(e -> e instanceof Node)
            .count());
    assertEquals(3,
        new XmlEntityReader(TestFiles.dataOsmXml())
            .read()
            .filter(e -> e instanceof Way)
            .count());
    assertEquals(1,
        new XmlEntityReader(TestFiles.dataOsmXml())
            .read()
            .filter(e -> e instanceof Relation)
            .count());
  }

}