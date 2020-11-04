package com.baremaps.osm;

import com.baremaps.osm.pbf.PbfEntityReader;
import com.baremaps.osm.xml.XmlChangeReader;
import com.baremaps.osm.xml.XmlEntityReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class OpenStreetMap {

  private OpenStreetMap() {

  }

  public static EntityReader newEntityReader(Path path) throws IOException {
    if (path.toString().endsWith(".pbf")) {
      return new PbfEntityReader(new BufferedInputStream(Files.newInputStream(path)));
    } else if (path.toString().endsWith(".xml")) {
      return new XmlEntityReader(new BufferedInputStream(Files.newInputStream(path)));
    } else if (path.toString().endsWith(".xml.gz")) {
      return new XmlEntityReader(new GZIPInputStream(new BufferedInputStream(Files.newInputStream(path))));
    } else {
      throw new IOException("Unrecognized file extension: " + path.getFileName());
    }
  }

  public static ChangeReader newChangeReader(Path path) throws IOException {
    if (path.toString().endsWith("osc")) {
      return new XmlChangeReader(new BufferedInputStream(Files.newInputStream(path)));
    } else {
      throw new IOException("Unrecognized file extension: " + path.getFileName());
    }
  }

}
