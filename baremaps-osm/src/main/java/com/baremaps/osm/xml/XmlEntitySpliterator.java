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

package com.baremaps.osm.xml;

import static javax.xml.stream.XMLInputFactory.IS_COALESCING;
import static javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE;
import static javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES;
import static javax.xml.stream.XMLInputFactory.IS_VALIDATING;
import static javax.xml.stream.XMLInputFactory.SUPPORT_DTD;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import com.baremaps.osm.domain.Bounds;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Info;
import com.baremaps.osm.domain.Member;
import com.baremaps.osm.domain.Member.MemberType;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XmlEntitySpliterator implements Spliterator<Entity> {

  private static final String ELEMENT_NAME_OSM = "osm";
  private static final String ELEMENT_NAME_BOUNDS = "bounds";
  private static final String ELEMENT_NAME_NODE = "node";
  private static final String ELEMENT_NAME_WAY = "way";
  private static final String ELEMENT_NAME_RELATION = "relation";
  private static final String ELEMENT_NAME_TAG = "tag";
  private static final String ELEMENT_NAME_NODE_REFERENCE = "nd";
  private static final String ELEMENT_NAME_MEMBER = "member";
  private static final String ATTRIBUTE_NAME_ID = "id";
  private static final String ATTRIBUTE_NAME_VERSION = "version";
  private static final String ATTRIBUTE_NAME_GENERATOR = "generator";
  private static final String ATTRIBUTE_NAME_TIMESTAMP = "timestamp";
  private static final String ATTRIBUTE_NAME_USER_ID = "uid";
  private static final String ATTRIBUTE_NAME_USER = "user";
  private static final String ATTRIBUTE_NAME_CHANGESET_ID = "changeset";
  private static final String ATTRIBUTE_NAME_LATITUDE = "lat";
  private static final String ATTRIBUTE_NAME_LONGITUDE = "lon";
  private static final String ATTRIBUTE_NAME_KEY = "k";
  private static final String ATTRIBUTE_NAME_VALUE = "v";
  private static final String ATTRIBUTE_NAME_REF = "ref";
  private static final String ATTRIBUTE_NAME_TYPE = "type";
  private static final String ATTRIBUTE_NAME_ROLE = "role";
  private static final String ATTRIBUTE_NAME_BOX = "box";
  private static final String ATTRIBUTE_NAME_ORIGIN = "origin";
  private static final String ATTRIBUTE_NAME_MAXLON = "maxlon";
  private static final String ATTRIBUTE_NAME_MAXLAT = "maxlat";
  private static final String ATTRIBUTE_NAME_MINLON = "minlon";
  private static final String ATTRIBUTE_NAME_MINLAT = "minlat";
  public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

  private final XMLStreamReader reader;

  public XmlEntitySpliterator(InputStream input) throws XMLStreamException {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(SUPPORT_DTD, false);
    factory.setProperty(IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    factory.setProperty(IS_NAMESPACE_AWARE, false);
    factory.setProperty(IS_VALIDATING, false);
    factory.setProperty(IS_COALESCING, false);
    this.reader = factory.createXMLStreamReader(input);
  }

  @Override
  public boolean tryAdvance(Consumer<? super Entity> consumer) {
    try {
      if (reader.hasNext()) {
        int event = reader.next();
        switch (event) {
          case START_ELEMENT:
            Entity entity = readEntity();
            consumer.accept(entity);
            return true;
          case END_DOCUMENT:
            return false;
          default:
            return true;
        }
      } else {
        return false;
      }
    } catch (XMLStreamException e) {
      e.printStackTrace();
      return false;
    }
  }

  private Entity readEntity() throws XMLStreamException {
    switch (reader.getLocalName()) {
      case ELEMENT_NAME_OSM:
        return readHeader();
      case ELEMENT_NAME_BOUNDS:
        return readBounds();
      case ELEMENT_NAME_NODE:
        return readNode();
      case ELEMENT_NAME_WAY:
        return readWay();
      case ELEMENT_NAME_RELATION:
        return readRelation();
      default:
        throw new RuntimeException("Unexpected XML element: " + reader.getLocalName());
    }
  }

  private Header readHeader() {
    String fileVersion = reader.getAttributeValue(null, ATTRIBUTE_NAME_VERSION);
    String generator = reader.getAttributeValue(null, ATTRIBUTE_NAME_GENERATOR);
    return new Header(null, null, null, "", generator);
  }

  private Bounds readBounds() throws XMLStreamException {
    double maxLon = Double.parseDouble(reader.getAttributeValue(null, ATTRIBUTE_NAME_MAXLON));
    double maxLat = Double.parseDouble(reader.getAttributeValue(null, ATTRIBUTE_NAME_MAXLAT));
    double minLon = Double.parseDouble(reader.getAttributeValue(null, ATTRIBUTE_NAME_MINLON));
    double minLat = Double.parseDouble(reader.getAttributeValue(null, ATTRIBUTE_NAME_MINLAT));
    reader.nextTag();
    return new Bounds(maxLat, maxLon, minLat, minLon);
  }

  private Node readNode() throws XMLStreamException {
    long id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_ID));
    Info info = readInfo();
    double latitude = Double.parseDouble(reader.getAttributeValue(null, ATTRIBUTE_NAME_LATITUDE));
    double longitude = Double.parseDouble(reader.getAttributeValue(null, ATTRIBUTE_NAME_LONGITUDE));

    // read the content of the node
    Map<String, String> tags = new HashMap<>();
    reader.nextTag();
    while (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
      switch (reader.getLocalName()) {
        case ELEMENT_NAME_TAG:
          readTag(tags);
          break;
        default:
          readUnknownElement();
          break;
      }
    }

    return new Node(id, info, tags, latitude, longitude);
  }

  private Way readWay() throws XMLStreamException {
    long id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_ID));
    Info info = readInfo();

    // read the content of the node
    Map<String, String> tags = new HashMap<>();
    List<Long> members = new ArrayList<>();
    reader.nextTag();
    while (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
      switch (reader.getLocalName()) {
        case ELEMENT_NAME_TAG:
          readTag(tags);
          break;
        case ELEMENT_NAME_NODE_REFERENCE:
          readWayMember(members);
          break;
        default:
          readUnknownElement();
          break;
      }
    }

    return new Way(id, info, tags, members);
  }

  private void readWayMember(List<Long> members) throws XMLStreamException {
    Long member = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_REF));
    members.add(member);
    reader.nextTag();
    reader.nextTag();
  }

  private Relation readRelation() throws XMLStreamException {
    long id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_ID));
    Info info = readInfo();

    // read the content of the node
    Map<String, String> tags = new HashMap<>();
    List<Member> members = new ArrayList<>();
    reader.nextTag();
    while (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
      switch (reader.getLocalName()) {
        case ELEMENT_NAME_TAG:
          readTag(tags);
          break;
        case ELEMENT_NAME_MEMBER:
          readRelationMember(members);
          break;
        default:
          readUnknownElement();
          break;
      }
    }

    return new Relation(id, info, tags, members);
  }

  private void readRelationMember(List<Member> members) throws XMLStreamException {
    long id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_REF));
    MemberType type = Member.MemberType.valueOf(reader.getAttributeValue(null, ATTRIBUTE_NAME_TYPE));
    String role = reader.getAttributeValue(null, ATTRIBUTE_NAME_ROLE);
    members.add(new Member(id, type, role));
    reader.nextTag();
    reader.nextTag();
  }

  private Info readInfo() {
    int version = Integer.parseInt(reader.getAttributeValue(null, ATTRIBUTE_NAME_VERSION));
    LocalDateTime timestamp = LocalDateTime.parse(reader.getAttributeValue(null, ATTRIBUTE_NAME_TIMESTAMP), format);
    long changeset = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_CHANGESET_ID));
    String uidValue = reader.getAttributeValue(null, ATTRIBUTE_NAME_USER_ID);
    int uid = uidValue != null ? Integer.parseInt(uidValue) : -1;
    return new Info(version, timestamp, changeset, uid);
  }

  private final void readTag(Map<String, String> tags) throws XMLStreamException {
    String name = reader.getAttributeValue(null, ATTRIBUTE_NAME_KEY);
    String value = reader.getAttributeValue(null, ATTRIBUTE_NAME_VALUE);
    tags.put(name, value);
    reader.nextTag();
    reader.nextTag();
  }

  private void readUnknownElement() throws XMLStreamException {
    int level = 0;
    do {
      if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
        level++;
      } else if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
        level--;
      }
      reader.nextTag();
    } while (level > 0);
  }


  @Override
  public Spliterator<Entity> trySplit() {
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
