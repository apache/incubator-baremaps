package io.gazetteer.osm.osmxml;

import static com.google.common.base.Preconditions.checkArgument;

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

  public static String path(long sequenceNumber) {
    checkArgument(sequenceNumber <= 999999999);
    String leading = String.format("%09d", sequenceNumber);
    return leading.substring(0, 3) + "/"
        + leading.substring(3, 6) + "/"
        + leading.substring(6, 9);
  }

  public static String changePath(long sequenceNumber) {
    return path(sequenceNumber) + ".osc.gz";
  }

  public static String statePath(long sequenceNumber) {
    return path(sequenceNumber) + ".state.txt";
  }

}
