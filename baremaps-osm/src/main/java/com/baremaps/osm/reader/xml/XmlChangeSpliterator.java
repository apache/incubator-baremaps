/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.osm.reader.xml;

import static com.baremaps.osm.model.User.NO_USER;

import com.baremaps.osm.model.Change;
import com.baremaps.osm.model.Change.Type;
import com.baremaps.osm.model.Member;
import com.baremaps.osm.model.Member.MemberType;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.User;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.stream.StreamException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XmlChangeSpliterator implements Spliterator<Change> {

  public static final String CREATE = "create";
  public static final String MODIFY = "modify";
  public static final String DELETE = "delete";
  public static final String USER = "user";
  public static final String NODE = "node";
  public static final String WAY = "way";
  public static final String RELATION = "relation";
  public static final String ID = "id";
  public static final String LON = "lon";
  public static final String LAT = "lat";
  public static final String VERSION = "version";
  public static final String TIMESTAMP = "timestamp";
  public static final String CHANGESET = "changeset";
  public static final String UID = "uid";
  public static final String TAG = "tag";
  public static final String MEMBER = "member";
  public static final String TYPE = "type";
  public static final String ROLE = "role";
  public static final String REF = "ref";
  public static final String ND = "nd";
  public static final String KEY = "k";
  public static final String VAL = "v";
  public static final DateTimeFormatter format;

  static {
    format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
  }

  private final XMLEventReader reader;

  private Type type;

  public XmlChangeSpliterator(XMLEventReader reader) {
    this.reader = reader;
  }

  public XmlChangeSpliterator(InputStream input) throws XMLStreamException {
    this(xmlEventReader(input));
  }

  private static XMLEventReader xmlEventReader(InputStream file) throws XMLStreamException {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
    factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
    factory.setProperty(XMLInputFactory.IS_COALESCING, false);
    return factory.createXMLEventReader(file);
  }

  private Node readNode(StartElement element)
      throws XMLStreamException {
    List<StartElement> children = readChildren(element);
    long id = Long.parseLong(element.getAttributeByName(QName.valueOf(ID)).getValue());
    int version = Integer.parseInt(element.getAttributeByName(QName.valueOf(VERSION)).getValue());
    LocalDateTime timestamp = LocalDateTime
        .parse(element.getAttributeByName(QName.valueOf(TIMESTAMP)).getValue(), format);
    long changeset = readChangeset(element);
    int userId = readUser(element).getId();
    Map<String, String> tags = readTags(children);
    double lat = Double.parseDouble(element.getAttributeByName(QName.valueOf(LAT)).getValue());
    double lon = Double.parseDouble(element.getAttributeByName(QName.valueOf(LON)).getValue());
    return new Node(id, version, timestamp, changeset, userId, tags, lon, lat);
  }

  private Way readWay(StartElement element)
      throws XMLStreamException {
    List<StartElement> children = readChildren(element);
    long id = Long.parseLong(element.getAttributeByName(QName.valueOf(ID)).getValue());
    int version = Integer.parseInt(element.getAttributeByName(QName.valueOf(VERSION)).getValue());
    LocalDateTime timestamp = LocalDateTime
        .parse(element.getAttributeByName(QName.valueOf(TIMESTAMP)).getValue(), format);
    long changeset = readChangeset(element);
    int userId = readUser(element).getId();
    Map<String, String> tags = readTags(children);
    List<Long> nodes = readNodes(children);
    return new Way(id, version, timestamp, changeset, userId, tags, nodes);
  }

  private Relation readRelation(StartElement element)
      throws XMLStreamException {
    List<StartElement> children = readChildren(element);
    long id = Long.parseLong(element.getAttributeByName(QName.valueOf(ID)).getValue());
    int version = Integer.parseInt(element.getAttributeByName(QName.valueOf(VERSION)).getValue());
    LocalDateTime timestamp = LocalDateTime
        .parse(element.getAttributeByName(QName.valueOf(TIMESTAMP)).getValue(), format);
    long changeset = readChangeset(element);
    int userId = readUser(element).getId();
    Map<String, String> tags = readTags(children);
    List<Member> members = readMembers(children);
    return new Relation(id, version, timestamp, changeset, userId, tags, members);
  }

  private List<StartElement> readChildren(StartElement element)
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

  private long readChangeset(StartElement element) {
    // TODO: changesets are not present on geofabrik but described as mandatory in the doc
    if (element.getAttributeByName(QName.valueOf(CHANGESET)) != null
        && element.getAttributeByName(QName.valueOf(CHANGESET)) != null) {
      return Long.parseLong(element.getAttributeByName(QName.valueOf(CHANGESET)).getValue());
    } else {
      return -1;
    }
  }

  private static User readUser(StartElement element) {
    if (element.getAttributeByName(QName.valueOf(UID)) != null
        && element.getAttributeByName(QName.valueOf(USER)) != null) {
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
        MemberType type = MemberType.valueOf(element.getAttributeByName(QName.valueOf(TYPE)).getValue());
        String role = element.getAttributeByName(QName.valueOf(ROLE)).getValue();
        nodes.add(new Member(ref, type, role));
      }
    }
    return nodes;
  }

  @Override
  public boolean tryAdvance(Consumer<? super Change> consumer) {
    try {
      while (reader.hasNext()) {
        XMLEvent event = reader.nextEvent();
        if (event.isStartElement()) {
          StartElement startElement = event.asStartElement();
          String name = startElement.getName().getLocalPart();
          if (CREATE.equals(name) || MODIFY.equals(name) || DELETE.equals(name)) {
            type = Type.valueOf(name);
          }
          switch (name) {
            case NODE:
              consumer.accept(new Change(type, readNode(startElement)));
              return true;
            case WAY:
              consumer.accept(new Change(type, readWay(startElement)));
              return true;
            case RELATION:
              consumer.accept(new Change(type, readRelation(startElement)));
              return true;
            default:
              break;
          }
        }
      }
      return false;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  @Override
  public Spliterator<Change> trySplit() {
    return null;
  }

  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return ORDERED | DISTINCT | NONNULL | IMMUTABLE;
  }

}
