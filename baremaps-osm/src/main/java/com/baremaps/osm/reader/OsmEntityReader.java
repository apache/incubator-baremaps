package com.baremaps.osm.reader;

import com.baremaps.osm.reader.pbf.PbfEntityReader;
import com.baremaps.osm.reader.xml.XmlEntityReader;
import com.baremaps.osm.reader.xml.XmlGzipEntityReader;
import java.nio.file.Path;

public class OsmEntityReader implements EntityReader{

  @Override
  public void read(Path path, EntityHandler handler) throws ReaderException {
    String file = path.getFileName().toString();
    if (file.endsWith(".pbf")) {
      new PbfEntityReader().read(path, handler);
    } else if (file.endsWith(".xml")) {
      new XmlEntityReader().read(path, handler);
    } else if (file.endsWith(".osm")) {
      new XmlEntityReader().read(path, handler);
    } else if (file.endsWith(".gz")) {
      new XmlGzipEntityReader().read(path, handler);
    } else {
      throw new ReaderException(new StringBuilder().append("Unsupported file format: ").append(file).toString());
    }
  }
}
