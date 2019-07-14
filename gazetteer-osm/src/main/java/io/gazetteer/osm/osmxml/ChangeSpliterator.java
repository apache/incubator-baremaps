package io.gazetteer.osm.osmxml;

import static io.gazetteer.osm.osmxml.XMLConstants.CREATE;
import static io.gazetteer.osm.osmxml.XMLConstants.DELETE;
import static io.gazetteer.osm.osmxml.XMLConstants.MODIFY;
import static io.gazetteer.osm.osmxml.XMLConstants.NODE;
import static io.gazetteer.osm.osmxml.XMLConstants.RELATION;
import static io.gazetteer.osm.osmxml.XMLConstants.WAY;

import io.gazetteer.osm.model.Change;
import io.gazetteer.osm.util.StreamException;
import java.util.Spliterator;
import java.util.function.Consumer;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

public class ChangeSpliterator implements Spliterator<Change> {

  private final XMLEventReader reader;

  private String type;

  public ChangeSpliterator(XMLEventReader reader) {
    this.reader = reader;
  }

  @Override
  public boolean tryAdvance(Consumer<? super Change> consumer) {
    try {
      while (reader.hasNext()) {
        XMLEvent event = reader.nextEvent();
        if (event.isStartElement()) {
          String name = event.asStartElement().getName().getLocalPart();
          if (CREATE.equals(name) || MODIFY.equals(name) || DELETE.equals(name)) {
            type = name;
          }
          switch (name) {
            case NODE:
              consumer.accept(new Change(type, XMLUtil.readNode(event.asStartElement(), reader)));
              return true;
            case WAY:
              consumer.accept(new Change(type, XMLUtil.readWay(event.asStartElement(), reader)));
              return true;
            case RELATION:
              consumer.accept(new Change(type, XMLUtil.readRelation(event.asStartElement(), reader)));
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
