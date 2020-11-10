package com.baremaps.osm;

import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.pbf.PbfEntityReader;
import com.baremaps.osm.xml.XmlChangeReader;
import com.baremaps.osm.xml.XmlEntityReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 * Utility methods for creating readers and streams from OpenStreetMap files.
 */
public class OpenStreetMap {

  private OpenStreetMap() {

  }

  public static Stream<Entity> entityStream(Path path) throws IOException {
    return entityReader(path).read();
  }

  public static Stream<Entity> entityStream(Path path, boolean parallel) throws IOException {
    return entityReader(path, parallel).read();
  }

  public static EntityReader entityReader(Path path) throws IOException {
    return entityReader(path, false);
  }

  public static EntityReader entityReader(Path path, boolean parallel) throws IOException {
    if (path.toString().endsWith(".pbf")) {
      return new PbfEntityReader(new BufferedInputStream(Files.newInputStream(path)), parallel);
    } else if (path.toString().endsWith(".xml")
        || path.toString().endsWith(".osm")) {
      return new XmlEntityReader(new BufferedInputStream(Files.newInputStream(path)), parallel);
    } else if (path.toString().endsWith(".xml.gz")
        || path.toString().endsWith(".osm.gz")) {
      return new XmlEntityReader(new GZIPInputStream(new BufferedInputStream(Files.newInputStream(path))), parallel);
    } else if (path.toString().endsWith((".xml.bz2"))
        || path.toString().endsWith(".osm.bz2")) {
      return new XmlEntityReader(new BZip2CompressorInputStream(new BufferedInputStream(Files.newInputStream(path))), parallel);
    } else {
      throw new IOException("Unrecognized file extension: " + path.getFileName());
    }
  }

  public static Stream<Change> changeStream(Path path) throws IOException {
    return changeReader(path).read();
  }

  public static Stream<Change> changeStream(Path path, boolean parallel) throws IOException {
    return changeReader(path, parallel).read();
  }

  public static ChangeReader changeReader(Path path) throws IOException {
    return changeReader(path, false);
  }

  public static ChangeReader changeReader(Path path, boolean parallel) throws IOException {
    if (path.toString().endsWith("osc")) {
      return new XmlChangeReader(new BufferedInputStream(Files.newInputStream(path)), parallel);
    } else {
      throw new IOException("Unrecognized file extension: " + path.getFileName());
    }
  }

}
