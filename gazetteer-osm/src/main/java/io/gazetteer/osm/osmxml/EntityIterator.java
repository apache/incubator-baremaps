package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.model.Entity;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class EntityIterator implements Iterator<Entity> {

  protected final XMLEventReader reader;

  public EntityIterator(XMLEventReader reader) {
    this.reader = reader;
  }

  @Override
  public boolean hasNext() {
    try {
      while (reader.hasNext()) {
        XMLEvent event = reader.peek();
        if (XmlUtil.isElement(event, XmlUtil.NODE)
            || XmlUtil.isElement(event, XmlUtil.WAY)
            || XmlUtil.isElement(event, XmlUtil.RELATION)) {
          return true;
        } else {
          reader.nextEvent();
        }
      }
      return false;
    } catch (XMLStreamException e) {
      return false;
    }
  }

  @Override
  public Entity next() {
    try {
      while (reader.hasNext()) {
        XMLEvent event = reader.nextEvent();
        if (XmlUtil.isElement(event, XmlUtil.NODE)) {
          return XmlUtil.readNode(event.asStartElement(), reader);
        } else if (XmlUtil.isElement(event, XmlUtil.WAY)) {
          return XmlUtil.readWay(event.asStartElement(), reader);
        } else if (XmlUtil.isElement(event, XmlUtil.RELATION)) {
          return XmlUtil.readRelation(event.asStartElement(), reader);
        }
      }
      throw new NoSuchElementException();
    } catch (XMLStreamException e) {
      throw new NoSuchElementException(e.getMessage());
    } catch (ParseException e) {
      throw new NoSuchElementException(e.getMessage());
    }
  }
}
