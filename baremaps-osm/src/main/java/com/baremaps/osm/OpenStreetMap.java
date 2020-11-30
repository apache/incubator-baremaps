package com.baremaps.osm;

import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.State;
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

  public static Stream<Stream<Entity>> blobStream(Path path) throws IOException {
    return blobStream(path, false, false);
  }

  public static Stream<Stream<Entity>> blobStream(Path path, boolean parallel) throws IOException {
    return blobStream(path, parallel, false);
  }

  public static Stream<Stream<Entity>> blobStream(Path path, boolean parallel, boolean async) throws IOException {
    return new PbfEntityReader(new BufferedInputStream(Files.newInputStream(path)), parallel, async).blobStream();
  }

  public static Stream<Entity> entityStream(Path path) throws IOException {
    return entityReader(path).stream();
  }

  public static Stream<Entity> entityStream(Path path, boolean parallel) throws IOException {
    return entityReader(path, parallel).stream();
  }

  public static Stream<Entity> entityStream(Path path, boolean parallel, boolean async) throws IOException {
    return entityReader(path, parallel, async).stream();
  }

  public static EntityReader entityReader(Path path) throws IOException {
    return entityReader(path, false, false);
  }

  public static EntityReader entityReader(Path path, boolean parallel) throws IOException {
    return entityReader(path, parallel, false);
  }

  private static EntityReader entityReader(Path path, boolean parallel, boolean async) throws IOException {
    if (path.toString().endsWith(".pbf")) {
      return new PbfEntityReader(new BufferedInputStream(Files.newInputStream(path)), parallel, async);
    } else if (path.toString().endsWith(".xml")
        || path.toString().endsWith(".osm")) {
      return new XmlEntityReader(new BufferedInputStream(Files.newInputStream(path)), parallel);
    } else if (path.toString().endsWith(".xml.gz")
        || path.toString().endsWith(".osm.gz")) {
      return new XmlEntityReader(new GZIPInputStream(new BufferedInputStream(Files.newInputStream(path))), parallel);
    } else if (path.toString().endsWith((".xml.bz2"))
        || path.toString().endsWith(".osm.bz2")) {
      return new XmlEntityReader(new BZip2CompressorInputStream(new BufferedInputStream(Files.newInputStream(path))),
          parallel);
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
    if (path.toString().endsWith("osc")
        || path.toString().endsWith("osc.xml")) {
      return new XmlChangeReader(new BufferedInputStream(Files.newInputStream(path)), parallel);
    } else if (path.toString().endsWith("osc.gz")) {
      return new XmlChangeReader(new GZIPInputStream(new BufferedInputStream(Files.newInputStream(path))), parallel);
    } else if (path.toString().endsWith("osc.bz2")) {
      return new XmlChangeReader(new BZip2CompressorInputStream(new BufferedInputStream(Files.newInputStream(path))),
          parallel);
    } else {
      throw new IOException("Unrecognized file extension: " + path.getFileName());
    }
  }

  public static State state(Path path) throws IOException {
    return stateReader(path).read();
  }

  public static StateReader stateReader(Path path) throws IOException {
    return new StateReader(new BufferedInputStream(Files.newInputStream(path)));
  }

}
