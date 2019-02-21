package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.model.Change;
import io.gazetteer.osm.util.BatchSpliterator;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Spliterator;

import static io.gazetteer.osm.osmxml.XMLUtil.xmlEventReader;

public class ChangeUtil {

  public static Iterator<Change> iterator(InputStream input) throws XMLStreamException {
    return new ChangeIterator(xmlEventReader(input));
  }

  public static Spliterator<Change> spliterator(InputStream input) throws XMLStreamException {
    return new BatchSpliterator<>(iterator(input), 10);
  }
}
