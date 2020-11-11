package com.baremaps.osm.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.osm.TestFiles;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class XmlChangeReaderTest {

  @Test
  void read() throws IOException {
    assertEquals(7, new XmlChangeReader(TestFiles.dataOscXml())
        .read()
        .count());
  }
}