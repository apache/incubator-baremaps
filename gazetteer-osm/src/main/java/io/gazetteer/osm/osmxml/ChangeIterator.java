package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Change;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ChangeIterator implements Iterator<Change> {

  private final XMLEventReader reader;

  private Change.Type type;

  public ChangeIterator(XMLEventReader reader) {
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
          updateType(reader.nextEvent());
        }
      }
      return false;
    } catch (XMLStreamException e) {
      return false;
    }
  }

  @Override
  public Change next() {
    try {
      while (reader.hasNext()) {
        XMLEvent event = reader.nextEvent();
        if (XMLUtil.isElement(event, XMLUtil.NODE)) {
          return new Change(type, XMLUtil.readNode(event.asStartElement(), reader));
        } else if (XMLUtil.isElement(event, XMLUtil.WAY)) {
          return new Change(type, XMLUtil.readWay(event.asStartElement(), reader));
        } else if (XMLUtil.isElement(event, XMLUtil.RELATION)) {
          return new Change(type, XMLUtil.readRelation(event.asStartElement(), reader));
        }
      }
      throw new NoSuchElementException();
    } catch (XMLStreamException e) {
      throw new NoSuchElementException(e.getMessage());
    } catch (ParseException e) {
      throw new NoSuchElementException(e.getMessage());
    }
  }

  private void updateType(XMLEvent event) throws XMLStreamException {
    if (XMLUtil.isElement(event, Change.Type.create.name())) {
      type = Change.Type.create;
    } else if (XMLUtil.isElement(event, Change.Type.modify.name())) {
      type = Change.Type.modify;
    } else if (XMLUtil.isElement(event, Change.Type.delete.name())) {
      type = Change.Type.delete;
    }
  }
}
