package com.baremaps.osm.xml;

import com.baremaps.osm.model.Entity;
import com.baremaps.osm.EntityReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.xml.stream.XMLStreamException;

public class XmlEntityReader implements EntityReader {

  private final InputStream inputStream;

  public XmlEntityReader(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  @Override
  public Stream<Entity> entities() throws IOException {
    try {
      Spliterator<Entity> spliterator = new XmlEntitySpliterator(inputStream);
      return StreamSupport.stream(spliterator, false);
    } catch (XMLStreamException e) {
      throw new IOException(e.getCause());
    }
  }
}
