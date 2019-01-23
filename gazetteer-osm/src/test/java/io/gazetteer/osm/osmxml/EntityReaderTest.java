package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.*;
import org.junit.Test;

import javax.xml.stream.XMLEventReader;

import java.io.EOFException;
import java.util.Arrays;

import static io.gazetteer.osm.OSMTestUtil.XML_DATA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EntityReaderTest {

    @Test(expected = EOFException.class)
    public void read() throws Exception {
        EntityReader entityReader = new EntityReader(XMLUtil.xmlEventReader(XML_DATA));
        for (int i = 0; i < 10; i++) {
            assertNotNull(entityReader.read());
        }
        entityReader.read();
    }

    @Test
    public void isElement() {
    }


    @Test
    public void readNode() throws Exception {
        XMLEventReader reader = XMLUtil.xmlEventReader(XML_DATA);
        while (reader.hasNext()) {
            if (reader.peek().isStartElement() && reader.peek().asStartElement().getName().getLocalPart().equals("node")) {
                Node node = EntityReader.readNode(reader);
                assertEquals(1, node.getInfo().getId());
                assertEquals(10, node.getInfo().getVersion());
                assertEquals(1199243045000l, node.getInfo().getTimestamp());
                assertEquals(10, node.getInfo().getUser().getId());
                assertEquals("user10", node.getInfo().getUser().getName());
                assertEquals(11, node.getInfo().getChangeset());
                assertEquals(-1, node.getLat(), 0);
                assertEquals(-2, node.getLon(), 0);
                assertEquals("Me1", node.getInfo().getTags().get("created_by"));
                break;
            }
            reader.nextEvent();
        }
    }

    @Test
    public void readWay() throws Exception {
        XMLEventReader reader = XMLUtil.xmlEventReader(XML_DATA);
        while (reader.hasNext()) {
            if (reader.peek().isStartElement() && reader.peek().asStartElement().getName().getLocalPart().equals("way")) {
                Way way = EntityReader.readWay(reader);
                assertEquals(1, way.getInfo().getId());
                assertEquals(10, way.getInfo().getVersion());
                assertEquals(1199243045000l, way.getInfo().getTimestamp());
                assertEquals(10, way.getInfo().getUser().getId());
                assertEquals("user10", way.getInfo().getUser().getName());
                assertEquals(11, way.getInfo().getChangeset());
                assertEquals(Arrays.asList(1l, 2l, 3l), way.getNodes());
                assertEquals("Me1", way.getInfo().getTags().get("created_by"));
                break;
            }
            reader.nextEvent();
        }
    }

    @Test
    public void readRelation() throws Exception {
        XMLEventReader reader = XMLUtil.xmlEventReader(XML_DATA);
        while (reader.hasNext()) {
            if (reader.peek().isStartElement() && reader.peek().asStartElement().getName().getLocalPart().equals("relation")) {
                Relation relation = EntityReader.readRelation(reader);
                assertEquals(1, relation.getInfo().getId());
                assertEquals(10, relation.getInfo().getVersion());
                assertEquals(1199243045000l, relation.getInfo().getTimestamp());
                assertEquals(10, relation.getInfo().getUser().getId());
                assertEquals("user10", relation.getInfo().getUser().getName());
                assertEquals(11, relation.getInfo().getChangeset());
                assertEquals(6, relation.getMembers().get(0).getId());
                assertEquals(Member.Type.node, relation.getMembers().get(0).getType());
                assertEquals("noderole", relation.getMembers().get(0).getRole());
                break;
            }
            reader.nextEvent();
        }
    }

}

