package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Entity;

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
        if (XMLUtil.isElement(event, XMLUtil.NODE)
            || XMLUtil.isElement(event, XMLUtil.WAY)
            || XMLUtil.isElement(event, XMLUtil.RELATION)) {
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
        if (XMLUtil.isElement(event, XMLUtil.NODE)) {
          return XMLUtil.readNode(event.asStartElement(), reader);
        } else if (XMLUtil.isElement(event, XMLUtil.WAY)) {
          return XMLUtil.readWay(event.asStartElement(), reader);
        } else if (XMLUtil.isElement(event, XMLUtil.RELATION)) {
          return XMLUtil.readRelation(event.asStartElement(), reader);
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
