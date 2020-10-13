package com.baremaps.osm.reader.xml;

import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.reader.EntityHandler;
import com.baremaps.osm.reader.EntityReader;
import com.baremaps.osm.reader.ReaderException;
import com.baremaps.osm.stream.StreamException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.xml.stream.XMLStreamException;

public class XmlEntityReader implements EntityReader {

  protected InputStream open(Path path) throws IOException {
    return Files.newInputStream(path);
  }

  @Override
  public void read(Path path, EntityHandler handler) throws ReaderException {
    try (InputStream inputStream = open(path)) {
      Spliterator<Object> spliterator = new XmlEntitySpliterator(inputStream);
      Stream<Object> stream = StreamSupport.stream(spliterator, false);
      stream.forEach(entity -> {
        try {
          handle(entity, handler);
        } catch (Exception e) {
          throw new StreamException(e);
        }
      });
    }  catch (StreamException e) {
      throw new ReaderException(e.getCause());
    } catch (XMLStreamException | IOException e) {
      throw new ReaderException(e);
    }
  }

  private void handle(Object object, EntityHandler handler) throws Exception {
    if (object instanceof Header) {
      Header header = (Header) object;
      handler.onHeader(header);
    } else if (object instanceof Node) {
      Node node = (Node) object;
      handler.onNode(node);
    } else if (object instanceof Way) {
      Way way = (Way) object;
      handler.onWay(way);
    } else if (object instanceof Relation) {
      Relation relation = (Relation) object;
      handler.onRelation(relation);
    }
  }


}
