package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.osmxml.ChangeReader;
import io.gazetteer.osm.osmxml.XMLUtil;
import org.junit.Test;

import static io.gazetteer.osm.OSMTestUtil.OSC_XML_DATA;
import static org.junit.Assert.assertNotNull;

public class ChangeReaderTest {

    @Test
    public void read() throws Exception {
        ChangeReader changeReader = new ChangeReader(XMLUtil.xmlEventReader(OSC_XML_DATA));
        for (int i = 0; i < 10; i++) {
            assertNotNull(changeReader.read());
        }
        changeReader.read();
    }
}