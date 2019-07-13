package io.gazetteer.osm.osmxml;

import static io.gazetteer.osm.osmxml.XmlUtil.xmlEventReader;

import io.gazetteer.osm.model.Entity;
import java.io.InputStream;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.xml.stream.XMLStreamException;

public class EntityUtil {

  public static Spliterator<Entity> spliterator(InputStream input) throws XMLStreamException {
    return new EntitySpliterator(xmlEventReader(input));
  }

  public static Stream<Entity> stream(InputStream input) throws XMLStreamException {
    return StreamSupport.stream(spliterator(input), false);
  }

}
