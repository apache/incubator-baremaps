package com.baremaps.osm.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.osm.DataFiles;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class XmlChangeReaderTest {

  @Test
  void read() throws IOException {
    assertEquals(7, new XmlChangeReader(DataFiles.dataOscXml())
        .read()
        .count());
  }
}