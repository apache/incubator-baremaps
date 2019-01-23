package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Member;
import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.domain.Relation;
import io.gazetteer.osm.domain.Way;
import org.junit.Test;

import javax.xml.stream.XMLEventReader;

import java.util.Arrays;

import static io.gazetteer.osm.OSMTestUtil.XML_DATA;
import static org.junit.Assert.assertEquals;

public class XMLFileReaderTest {

    @Test
    public void readNode() throws Exception {
        XMLEventReader reader = XMLFileUtil.reader(XML_DATA);
        while (reader.hasNext()) {
            if (reader.peek().isStartElement() && reader.peek().asStartElement().getName().getLocalPart().equals("node")) {
                Node node = XMLFileReader.readNode(reader);
                assertEquals(1, node.getInfo().getId());
                assertEquals(10, node.getInfo().getVersion());
                assertEquals(1199243045000l, node.getInfo().getTimestamp());
                assertEquals(10, node.getInfo().getUser().getId());
                assertEquals("user10", node.getInfo().getUser().getName());
                assertEquals(11, node.getInfo().getChangeset());
                assertEquals(-1, node.getLat(), 0);
                assertEquals(-2, node.getLon(), 0);
                break;
            }
            reader.nextEvent();
        }
    }

    @Test
    public void readWay() throws Exception {
        XMLEventReader reader = XMLFileUtil.reader(XML_DATA);
        while (reader.hasNext()) {
            if (reader.peek().isStartElement() && reader.peek().asStartElement().getName().getLocalPart().equals("way")) {
                Way way = XMLFileReader.readWay(reader);
                assertEquals(1, way.getInfo().getId());
                assertEquals(10, way.getInfo().getVersion());
                assertEquals(1199243045000l, way.getInfo().getTimestamp());
                assertEquals(10, way.getInfo().getUser().getId());
                assertEquals("user10", way.getInfo().getUser().getName());
                assertEquals(11, way.getInfo().getChangeset());
                assertEquals(Arrays.asList(1l, 2l, 3l), way.getNodes());
                break;
            }
            reader.nextEvent();
        }
    }

    @Test
    public void readRelation() throws Exception {
        XMLEventReader reader = XMLFileUtil.reader(XML_DATA);
        while (reader.hasNext()) {
            if (reader.peek().isStartElement() && reader.peek().asStartElement().getName().getLocalPart().equals("relation")) {
                Relation relation = XMLFileReader.readRelation(reader);
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

    @Test
    public void readTags() {
    }

}

