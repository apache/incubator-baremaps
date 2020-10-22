package com.baremaps.osm.reader.xml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class XmlGzipEntityReader extends XmlEntityReader {

  public InputStream open(Path path) throws IOException {
    return new GZIPInputStream(Files.newInputStream(path));
  }

}
