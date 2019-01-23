package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.*;
import org.junit.Test;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import java.io.EOFException;
import java.util.Arrays;

import static io.gazetteer.osm.OSMTestUtil.OSM_XML_DATA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EntityIteratorTest {

//    @Test(expected = EOFException.class)
//    public void read() throws Exception {
//        EntityIterator entityIterator = new EntityIterator(XMLUtil.xmlEventReader(OSM_XML_DATA));
//        for (int i = 0; i < 10; i++) {
//            assertNotNull(entityIterator.read());
//        }
//        entityIterator.read();
//    }



    @Test
    public void hasNext() {
    }

    @Test
    public void next() {
    }
}

