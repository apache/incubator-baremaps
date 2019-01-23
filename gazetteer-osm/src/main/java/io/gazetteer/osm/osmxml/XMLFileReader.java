package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.*;

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

import static io.gazetteer.osm.domain.User.NO_USER;

public class XMLFileReader {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static String NODE = "node";

    private static String WAY = "way";

    private static String RELATION = "relation";

    static {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    static public void main(String[] args) throws Exception {
        String fileName = args[0];
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, false);
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        XMLEventReader reader = factory.createXMLEventReader(new FileInputStream(fileName));
        while (reader.hasNext()) {
            XMLEvent event = reader.peek();
            if (event.isStartElement()) {
                String element = event.asStartElement().getName().getLocalPart();
                if (element.equals(NODE)) {
                    Node node = readNode(reader);
                } else if (element.equals(WAY)) {
                    readWay(reader);
                } else if (element.equals(RELATION)) {
                    readRelation(reader);
                } else {
                    reader.nextEvent();
                }
            } else {
                reader.nextEvent();
            }
        }
    }

    public static Node readNode(XMLEventReader reader) throws XMLStreamException, ParseException {
        XMLEvent event = reader.nextEvent();
        StartElement node = event.asStartElement();
        long id = Long.parseLong(node.getAttributeByName(QName.valueOf("id")).getValue());
        double lat = Double.parseDouble(node.getAttributeByName(QName.valueOf("lat")).getValue());
        double lon = Double.parseDouble(node.getAttributeByName(QName.valueOf("lon")).getValue());
        int version = Integer.parseInt(node.getAttributeByName(QName.valueOf("version")).getValue());
        long timestamp = format.parse(node.getAttributeByName(QName.valueOf("timestamp")).getValue()).getTime();
        long changeset = Long.parseLong(node.getAttributeByName(QName.valueOf("changeset")).getValue());
        User user = NO_USER;
        Map<String, String> tags = new HashMap<>();
        while (!(reader.peek().isEndElement() && reader.peek().asEndElement().getName().getLocalPart().equals("node"))) {
            XMLEvent child = reader.nextEvent();
            if (child.isStartElement() && child.asStartElement().getName().getLocalPart().equals("tag")) {
                StartElement tag = child.asStartElement();
                String key = tag.getAttributeByName(QName.valueOf("k")).getValue();
                String val = tag.getAttributeByName(QName.valueOf("v")).getValue();
                tags.put(key, val);
            }
        }
        return new Node(new Info(id, version, timestamp, changeset, user, tags), lon, lat);
    }

    public static Way readWay(XMLEventReader reader) throws XMLStreamException, ParseException {
        return null;
    }

    public static Relation readRelation(XMLEventReader reader) throws XMLStreamException, ParseException {
        return null;
    }

    public static HashMap<String, String> readTags(XMLEventReader reader) {
        return null;
    }


}
