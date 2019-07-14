package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.model.Change;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Spliterator;

public class ChangeUtil {

  public static Spliterator<Change> spliterator(InputStream input) throws XMLStreamException {
    return new ChangeSpliterator(XMLUtil.xmlEventReader(input));
  }

  public static Stream<Change> stream(InputStream input) throws XMLStreamException {
    return StreamSupport.stream(spliterator(input), false);
  }

}
