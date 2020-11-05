package com.baremaps.osm.xml;

import com.baremaps.osm.EntityReader;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.stream.BatchSpliterator;
import java.io.IOException;
import java.io.InputStream;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.xml.stream.XMLStreamException;

public class XmlEntityReader implements EntityReader {

  private final InputStream inputStream;

  private final boolean parallel;

  public XmlEntityReader(InputStream inputStream) {
    this(inputStream, false);
  }

  public XmlEntityReader(InputStream inputStream, boolean parallel) {
    this.inputStream = inputStream;
    this.parallel = parallel;
  }

  @Override
  public Stream<Entity> read() throws IOException {
    try {
      Spliterator<Entity> spliterator = new XmlEntitySpliterator(inputStream);
      if (parallel) {
        spliterator = new BatchSpliterator<>(spliterator, 1000);
      }
      return StreamSupport.stream(spliterator, parallel);
    } catch (XMLStreamException e) {
      throw new IOException(e.getCause());
    }
  }
}
