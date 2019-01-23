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

    private static final String NODE = "node";
    private static final String WAY = "way";
    private static final String RELATION = "relation";
    private static final String LAT = "lat";
    private static final String LON = "lon";
    private static final String ID = "id";
    private static final String VERSION = "version";
    private static final String TIMESTAMP = "timestamp";
    private static final String CHANGESET = "changeset";
    private static final String UID = "uid";
    private static final String USER = "user";
    private static final String TAG = "tag";
    private static final String KEY = "k";
    private static final String VAL = "v";
    private static final String ND = "nd";
    private static final String REF = "ref";
    private static final String MEMBER = "member";
    private static final String TYPE = "type";
    private static final String ROLE = "role";

    private static final SimpleDateFormat format ;

    static {
        format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static Node readNode(XMLEventReader reader) throws XMLStreamException, ParseException {
        checkState(checkReaderElement(reader, NODE));
        StartElement element = readElement(reader);
        List<StartElement> children = readChildren(element, reader);
        Info info = readInfo(element, children);
        double lat = Double.parseDouble(element.getAttributeByName(QName.valueOf(LAT)).getValue());
        double lon = Double.parseDouble(element.getAttributeByName(QName.valueOf(LON)).getValue());
        return new Node(info, lon, lat);
    }

    public static Way readWay(XMLEventReader reader) throws XMLStreamException, ParseException {
        checkState(checkReaderElement(reader, WAY));
        StartElement element = readElement(reader);
        List<StartElement> children = readChildren(element, reader);
        Info info = readInfo(element, children);
        List<Long> nodes = readNodes(children);
        return new Way(info, nodes);
    }

    public static Relation readRelation(XMLEventReader reader) throws XMLStreamException, ParseException {
        checkState(checkReaderElement(reader, RELATION));
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
        long id = Long.parseLong(element.getAttributeByName(QName.valueOf(ID)).getValue());
        int version = Integer.parseInt(element.getAttributeByName(QName.valueOf(VERSION)).getValue());
        long timestamp = format.parse(element.getAttributeByName(QName.valueOf(TIMESTAMP)).getValue()).getTime();
        long changeset = Long.parseLong(element.getAttributeByName(QName.valueOf(CHANGESET)).getValue());
        User user = readUser(element);
        Map<String, String> tags = readTags(children);
        return new Info(id, version, timestamp, changeset, user, tags);
    }

    private static User readUser(StartElement element) {
        if (element.getAttributeByName(QName.valueOf(UID)) != null && element.getAttributeByName(QName.valueOf(USER)) != null) {
            int uid = Integer.parseInt(element.getAttributeByName(QName.valueOf(UID)).getValue());
            String name = element.getAttributeByName(QName.valueOf(USER)).getValue();
            return new User(uid, name);
        } else {
            return NO_USER;
        }
    }

    private static Map<String, String> readTags(List<StartElement> elements) {
        Map<String, String> tags = new HashMap<>();
        for (StartElement element : elements) {
            if (element.getName().getLocalPart().equals(TAG)) {
                String key = element.getAttributeByName(QName.valueOf(KEY)).getValue();
                String val = element.getAttributeByName(QName.valueOf(VAL)).getValue();
                tags.put(key, val);
            }
        }
        return tags;
    }

    private static List<Long> readNodes(List<StartElement> elements) {
        List<Long> nodes = new ArrayList<>();
        for (StartElement element : elements) {
            if (element.getName().getLocalPart().equals(ND)) {
                long ref = Long.parseLong(element.getAttributeByName(QName.valueOf(REF)).getValue());
                nodes.add(ref);
            }
        }
        return nodes;
    }

    private static List<Member> readMembers(List<StartElement> elements) {
        List<Member> nodes = new ArrayList<>();
        for (StartElement element : elements) {
            if (element.getName().getLocalPart().equals(MEMBER)) {
                long ref = Long.parseLong(element.getAttributeByName(QName.valueOf(REF)).getValue());
                String type = element.getAttributeByName(QName.valueOf(TYPE)).getValue();
                String role = element.getAttributeByName(QName.valueOf(ROLE)).getValue();
                nodes.add(new Member(ref, Member.Type.valueOf(type), role));
            }
        }
        return nodes;
    }

}
