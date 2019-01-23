package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.base.Preconditions.checkState;
import static io.gazetteer.osm.domain.User.NO_USER;

public class XMLFileReader {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static Node readNode(XMLEventReader reader) throws XMLStreamException, ParseException {
        checkState(checkReaderElement(reader,"node"));
        StartElement element = readElement(reader);
        List<StartElement> children = readChildren(element, reader);
        Info info = readInfo(element, children);
        double lat = Double.parseDouble(element.getAttributeByName(QName.valueOf("lat")).getValue());
        double lon = Double.parseDouble(element.getAttributeByName(QName.valueOf("lon")).getValue());
        return new Node(info, lon, lat);
    }

    public static Way readWay(XMLEventReader reader) throws XMLStreamException, ParseException {
        checkState(checkReaderElement(reader,"way"));
        StartElement element = readElement(reader);
        List<StartElement> children = readChildren(element, reader);
        Info info = readInfo(element, children);
        List<Long> nodes = readNodes(children);
        return new Way(info, nodes);
    }

    public static Relation readRelation(XMLEventReader reader) throws XMLStreamException, ParseException {
        checkState(checkReaderElement(reader,"relation"));
        StartElement element = readElement(reader);
        List<StartElement> children = readChildren(element, reader);
        Info info = readInfo(element, children);
        List<Member> members = readMembers(children);
        return new Relation(info, members);
    }

    private static boolean checkReaderElement(XMLEventReader reader, String element) throws XMLStreamException {
        return reader.peek().isStartElement() &&
                reader.peek().asStartElement().getName().getLocalPart().equals(element);
    }

    private static StartElement readElement(XMLEventReader reader) throws XMLStreamException {
        XMLEvent event = reader.nextEvent();
        return event.asStartElement();
    }

    private static List<StartElement> readChildren(StartElement element, XMLEventReader reader) throws XMLStreamException {
        List<StartElement> children = new ArrayList<>();
        while (!(reader.peek().isEndElement()
                && reader.peek().asEndElement().getName().getLocalPart().equals(element.getName().getLocalPart()))) {
            XMLEvent child = reader.nextEvent();
            if (child.isStartElement()) {
                children.add(child.asStartElement());
            }
        }
        return children;
    }

    private static Info readInfo(StartElement element, List<StartElement> children) throws ParseException {
        long id = Long.parseLong(element.getAttributeByName(QName.valueOf("id")).getValue());
        int version = Integer.parseInt(element.getAttributeByName(QName.valueOf("version")).getValue());
        long timestamp = format.parse(element.getAttributeByName(QName.valueOf("timestamp")).getValue()).getTime();
        long changeset = Long.parseLong(element.getAttributeByName(QName.valueOf("changeset")).getValue());
        User user = readUser(element);
        Map<String, String> tags = readTags(children);
        return new Info(id, version, timestamp, changeset, user, tags);
    }

    private static User readUser(StartElement element) {
        if (element.getAttributeByName(QName.valueOf("uid")) != null && element.getAttributeByName(QName.valueOf("user")) != null) {
            int uid = Integer.parseInt(element.getAttributeByName(QName.valueOf("uid")).getValue());
            String name = element.getAttributeByName(QName.valueOf("user")).getValue();
            return new User(uid, name);
        } else {
            return NO_USER;
        }
    }

    private static Map<String, String> readTags(List<StartElement> elements) {
        Map<String, String> tags = new HashMap<>();
        for (StartElement element : elements) {
            if (element.getName().getLocalPart().equals("tag")) {
                String key = element.getAttributeByName(QName.valueOf("k")).getValue();
                String val = element.getAttributeByName(QName.valueOf("v")).getValue();
                tags.put(key, val);
            }
        }
        return tags;
    }

    private static List<Long> readNodes(List<StartElement> elements) {
        List<Long> nodes = new ArrayList<>();
        for (StartElement element : elements) {
            if (element.getName().getLocalPart().equals("nd")) {
                long ref = Long.parseLong(element.getAttributeByName(QName.valueOf("ref")).getValue());
                nodes.add(ref);
            }
        }
        return nodes;
    }

    private static List<Member> readMembers(List<StartElement> elements) {
        List<Member> nodes = new ArrayList<>();
        for (StartElement element : elements) {
            if (element.getName().getLocalPart().equals("member")) {
                long ref = Long.parseLong(element.getAttributeByName(QName.valueOf("ref")).getValue());
                String type = element.getAttributeByName(QName.valueOf("type")).getValue();
                String role = element.getAttributeByName(QName.valueOf("role")).getValue();
                nodes.add(new Member(ref, Member.Type.valueOf(type), role));
            }
        }
        return nodes;
    }


}
