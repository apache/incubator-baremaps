package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Member;
import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.domain.Relation;
import io.gazetteer.osm.domain.Way;
import org.junit.Test;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;
import java.util.Arrays;

import static io.gazetteer.osm.OSMTestUtil.OSM_XML_DATA;
import static org.junit.Assert.assertEquals;

public class XMLUtilTest {

  @Test
  public void readNode() throws Exception {
    XMLEventReader reader = XMLUtil.xmlEventReader(OSM_XML_DATA);
    while (reader.hasNext()) {
      XMLEvent event = reader.nextEvent();
      if (event.isStartElement()
          && event.asStartElement().getName().getLocalPart().equals("node")) {
        Node node = XMLUtil.readNode(event.asStartElement(), reader);
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
    }
  }

  @Test
  public void readWay() throws Exception {
    XMLEventReader reader = XMLUtil.xmlEventReader(OSM_XML_DATA);
    while (reader.hasNext()) {
      XMLEvent event = reader.nextEvent();
      if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("way")) {
        Way way = XMLUtil.readWay(event.asStartElement(), reader);
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
    }
  }

  @Test
  public void readRelation() throws Exception {
    XMLEventReader reader = XMLUtil.xmlEventReader(OSM_XML_DATA);
    while (reader.hasNext()) {
      XMLEvent event = reader.nextEvent();
      if (event.isStartElement()
          && event.asStartElement().getName().getLocalPart().equals("relation")) {
        Relation relation = XMLUtil.readRelation(event.asStartElement(), reader);
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
    }
  }
}
