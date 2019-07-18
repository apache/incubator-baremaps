package io.gazetteer.osm.osmxml;

import static io.gazetteer.osm.osmxml.XMLConstants.NODE;
import static io.gazetteer.osm.osmxml.XMLConstants.RELATION;
import static io.gazetteer.osm.osmxml.XMLConstants.WAY;

import io.gazetteer.osm.model.Entity;

import io.gazetteer.common.stream.StreamException;
import java.util.Spliterator;
import java.util.function.Consumer;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

public class EntitySpliterator implements Spliterator<Entity> {

  private final XMLEventReader reader;

  public EntitySpliterator(XMLEventReader reader) {
    this.reader = reader;
  }

  @Override
  public boolean tryAdvance(Consumer<? super Entity> consumer) {
    try {
      while (reader.hasNext()) {
        XMLEvent event = reader.nextEvent();
        if (event.isStartElement()) {
          String name = event.asStartElement().getName().getLocalPart();
          switch (name) {
            case NODE:
              consumer.accept(XMLUtil.readNode(event.asStartElement(), reader));
              return true;
            case WAY:
              consumer.accept(XMLUtil.readWay(event.asStartElement(), reader));
              return true;
            case RELATION:
              consumer.accept(XMLUtil.readRelation(event.asStartElement(), reader));
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
