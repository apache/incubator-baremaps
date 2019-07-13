package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.model.Change;
import io.gazetteer.osm.util.BatchSpliterator;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Spliterator;

import static io.gazetteer.osm.osmxml.XmlUtil.xmlEventReader;

public class ChangeUtil {

  public static Iterator<Change> iterator(InputStream input) throws XMLStreamException {
    return new ChangeIterator(xmlEventReader(input));
  }

  public static Spliterator<Change> spliterator(InputStream input) throws XMLStreamException {
    return new BatchSpliterator<>(iterator(input), 10);
  }

  public static Stream<Change> stream(InputStream input) throws XMLStreamException {
    return StreamSupport.stream(spliterator(input), true);
  }
}
