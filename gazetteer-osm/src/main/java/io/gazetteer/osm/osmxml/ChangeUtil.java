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

  public static Spliterator<Change> spliterator(InputStream input) throws XMLStreamException {
    return new ChangeSpliterator(XmlUtil.xmlEventReader(input));
  }

  public static Stream<Change> stream(InputStream input) throws XMLStreamException {
    return StreamSupport.stream(spliterator(input), false);
  }

}
