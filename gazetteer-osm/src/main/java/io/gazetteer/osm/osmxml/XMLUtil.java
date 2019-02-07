package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.model.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.gazetteer.osm.model.User.NO_USER;

public class XMLUtil {

  protected static final SimpleDateFormat format;

  static {
    format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  protected static final String LON = "lon";
  protected static final String ID = "id";
  protected static final String VERSION = "version";
  protected static final String TIMESTAMP = "timestamp";
  protected static final String CHANGESET = "changeset";
  protected static final String UID = "uid";
  protected static final String USER = "user";
  protected static final String TAG = "tag";
  protected static final String KEY = "k";
  protected static final String VAL = "v";
  protected static final String ND = "nd";
  protected static final String REF = "ref";
  protected static final String MEMBER = "member";
  protected static final String TYPE = "type";
  protected static final String ROLE = "role";
  protected static final String NODE = "node";
  protected static final String WAY = "way";
  protected static final String RELATION = "relation";
  protected static final String LAT = "lat";

  public static XMLEventReader xmlEventReader(File file) throws Exception {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.IS_COALESCING, false);
    factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
    factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
    return factory.createXMLEventReader(new FileInputStream(file));
  }

  protected static boolean isElement(XMLEvent event, String element) throws XMLStreamException {
    return event.isStartElement()
        && event.asStartElement().getName().getLocalPart().equals(element);
  }

  protected static Node readNode(StartElement element, XMLEventReader reader)
      throws XMLStreamException, ParseException {
    List<StartElement> children = readChildren(element, reader);
    Info info = readInfo(element, children);
    double lat = Double.parseDouble(element.getAttributeByName(QName.valueOf(LAT)).getValue());
    double lon = Double.parseDouble(element.getAttributeByName(QName.valueOf(LON)).getValue());
    return new Node(info, lon, lat);
  }

  protected static Way readWay(StartElement element, XMLEventReader reader)
      throws XMLStreamException, ParseException {
    List<StartElement> children = readChildren(element, reader);
    Info info = readInfo(element, children);
    List<Long> nodes = readNodes(children);
    return new Way(info, nodes);
  }

  protected static Relation readRelation(StartElement element, XMLEventReader reader)
      throws XMLStreamException, ParseException {
    List<StartElement> children = readChildren(element, reader);
    Info info = readInfo(element, children);
    List<Member> members = readMembers(children);
    return new Relation(info, members);
  }

  protected static List<StartElement> readChildren(StartElement element, XMLEventReader reader)
      throws XMLStreamException {
    List<StartElement> children = new ArrayList<>();
    while (!(reader.peek().isEndElement()
        && reader
            .peek()
            .asEndElement()
            .getName()
            .getLocalPart()
            .equals(element.getName().getLocalPart()))) {
      XMLEvent child = reader.nextEvent();
      if (child.isStartElement()) {
        children.add(child.asStartElement());
      }
    }
    return children;
  }

  protected static Info readInfo(StartElement element, List<StartElement> children)
      throws ParseException {
    long id = Long.parseLong(element.getAttributeByName(QName.valueOf(ID)).getValue());
    int version = Integer.parseInt(element.getAttributeByName(QName.valueOf(VERSION)).getValue());
    long timestamp =
        format.parse(element.getAttributeByName(QName.valueOf(TIMESTAMP)).getValue()).getTime();
    long changeset = readChangeset(element);
    User user = readUser(element);
    Map<String, String> tags = readTags(children);
    return new Info(id, version, timestamp, changeset, user, tags);
  }

  protected static long readChangeset(StartElement element) {
    // todo: changesets are not present on geofabrik but described as mandatory in the doc
    if (element.getAttributeByName(QName.valueOf(CHANGESET)) != null
        && element.getAttributeByName(QName.valueOf(CHANGESET)) != null) {
      return Long.parseLong(element.getAttributeByName(QName.valueOf(CHANGESET)).getValue());
    } else {
      return -1;
    }
  }

  protected static User readUser(StartElement element) {
    if (element.getAttributeByName(QName.valueOf(UID)) != null
        && element.getAttributeByName(QName.valueOf(USER)) != null) {
      int uid = Integer.parseInt(element.getAttributeByName(QName.valueOf(UID)).getValue());
      String name = element.getAttributeByName(QName.valueOf(USER)).getValue();
      return new User(uid, name);
    } else {
      return NO_USER;
    }
  }

  protected static Map<String, String> readTags(List<StartElement> elements) {
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

  protected static List<Long> readNodes(List<StartElement> elements) {
    List<Long> nodes = new ArrayList<>();
    for (StartElement element : elements) {
      if (element.getName().getLocalPart().equals(ND)) {
        long ref = Long.parseLong(element.getAttributeByName(QName.valueOf(REF)).getValue());
        nodes.add(ref);
      }
    }
    return nodes;
  }

  protected static List<Member> readMembers(List<StartElement> elements) {
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
