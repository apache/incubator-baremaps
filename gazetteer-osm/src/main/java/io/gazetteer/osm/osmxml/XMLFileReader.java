package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Info;
import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.domain.User;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class XMLFileReader {

    static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    static public void main(String[] args) throws Exception {
        String fileName = args[0];
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        XMLEventReader xer = xmlInputFactory.createXMLEventReader(new FileInputStream(fileName));

        while (xer.hasNext()) {
            XMLEvent entity = xer.peek();
            if (entity.isStartElement() && entity.asStartElement().getName().getLocalPart().equals("node")) {
                Node node = readNode(xer);
                if (node.getInfo().getTags().size() > 0) {
                    System.out.println(node.getInfo().getId());
                }
            } else {
                xer.nextEvent();
            }
        }

    }


    public static Node readNode(XMLEventReader xer) throws XMLStreamException, ParseException {
        XMLEvent event = xer.nextEvent();
        StartElement node = event.asStartElement();
        long id = Long.parseLong(node.getAttributeByName(QName.valueOf("id")).getValue());
        double lat = Double.parseDouble(node.getAttributeByName(QName.valueOf("lat")).getValue());
        double lon = Double.parseDouble(node.getAttributeByName(QName.valueOf("lon")).getValue());
        int version = Integer.parseInt(node.getAttributeByName(QName.valueOf("version")).getValue());
        long timestamp = format.parse(node.getAttributeByName(QName.valueOf("timestamp")).getValue()).getTime();
        long changeset = Long.parseLong(node.getAttributeByName(QName.valueOf("changeset")).getValue());
        User user = new User(-1, "");
        Map<String, String> tags = new HashMap<>();
        while (!(xer.peek().isEndElement() && xer.peek().asEndElement().getName().getLocalPart().equals("node"))) {
            XMLEvent child = xer.nextEvent();
            if (child.isStartElement() && child.asStartElement().getName().getLocalPart().equals("tag")) {
                StartElement tag = child.asStartElement();
                String key = tag.getAttributeByName(QName.valueOf("k")).getValue();
                String val = tag.getAttributeByName(QName.valueOf("v")).getValue();
                tags.put(key, val);
            }
        }
        return new Node(new Info(id, version, timestamp, changeset, user, tags), lon, lat);
    }


}
