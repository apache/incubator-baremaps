package com.baremaps.osm;

import com.baremaps.osm.handler.BlockEntityHandler;
import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.State;
import com.baremaps.osm.pbf.BlobIterator;
import com.baremaps.osm.domain.Block;
import com.baremaps.osm.pbf.BlockReader;
import com.baremaps.osm.xml.XmlChangeSpliterator;
import com.baremaps.osm.xml.XmlEntitySpliterator;
import com.baremaps.osm.progress.InputStreamProgress;
import com.baremaps.osm.progress.ProgressLogger;
import com.baremaps.stream.StreamException;
import com.baremaps.stream.StreamUtils;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 * Utility methods for creating readers and streams from OpenStreetMap files.
 */
public class OpenStreetMap {

  private OpenStreetMap() {

  }

  private static InputStream newInputStream(Path path) throws IOException {
    ProgressLogger progressLogger = new ProgressLogger(Files.size(path), 5000);
    return new InputStreamProgress(new BufferedInputStream(Files.newInputStream(path)), progressLogger);
  }

  private static Stream<Entity> streamPbfBlockEntities(Block block) {
    try {
      Stream.Builder<Entity> entities = Stream.builder();
      block.handle(new BlockEntityHandler(entities::add));
      return entities.build();
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  public static Stream<Block> streamPbfBlocks(Path path, boolean parallel) throws IOException {
    return streamPbfBlocks(newInputStream(path), parallel);
  }

  public static Stream<Block> streamPbfBlocks(InputStream input, boolean parallel) {
    if (parallel) {
      return StreamUtils.batch(
          StreamUtils.bufferInCompletionOrder(
              StreamUtils.stream(new BlobIterator(input)),
              blob -> new BlockReader(blob).readBlock(),
              Runtime.getRuntime().availableProcessors()), 1);
    } else {
      return StreamUtils.bufferInSourceOrder(
          StreamUtils.stream(new BlobIterator(input)),
          blob -> new BlockReader(blob).readBlock(),
          Runtime.getRuntime().availableProcessors());
    }
  }

  public static Stream<Entity> streamPbfEntities(Path path, boolean parallel) throws IOException {
    return streamPbfBlocks(newInputStream(path), parallel)
        .flatMap(OpenStreetMap::streamPbfBlockEntities);
  }

  public static Stream<Entity> streamPbfEntities(InputStream input, boolean parallel) {
    return streamPbfBlocks(input, parallel).flatMap(OpenStreetMap::streamPbfBlockEntities);
  }

  public static Stream<Entity> streamXmlEntities(Path path, boolean parallel) throws IOException {
    return streamXmlEntities(newInputStream(path), parallel);
  }

  public static Stream<Entity> streamXmlEntities(InputStream input, boolean parallel) throws IOException {
    Stream<Entity> stream = StreamSupport.stream(new XmlEntitySpliterator(input), parallel);
    if (parallel) {
      stream = StreamUtils.batch(stream, 1000);
    }
    return stream;
  }

  public static Stream<Entity> streamEntities(Path path, boolean parallel) throws IOException {
    InputStream input = newInputStream(path);
    if (path.toString().endsWith(".pbf")) {
      return streamPbfEntities(input, parallel);
    } else if (path.toString().endsWith(".xml")
        || path.toString().endsWith(".osm")) {
      return streamXmlEntities(input, parallel);
    } else if (path.toString().endsWith(".xml.gz")
        || path.toString().endsWith(".osm.gz")) {
      return streamXmlEntities(new GZIPInputStream(input), parallel);
    } else if (path.toString().endsWith((".xml.bz2"))
        || path.toString().endsWith(".osm.bz2")) {
      return streamXmlEntities(new BZip2CompressorInputStream(input), parallel);
    } else {
      throw new IOException("Unrecognized file extension: " + path.getFileName());
    }
  }

  public static Stream<Change> streamXmlChanges(Path path, boolean parallel) throws XMLStreamException, IOException {
    if (path.toString().endsWith("osc")
        || path.toString().endsWith("osc.xml")) {
      return streamXmlChanges(newInputStream(path), parallel);
    } else if (path.toString().endsWith("osc.gz")) {
      return streamXmlChanges(new GZIPInputStream(newInputStream(path)), parallel);
    } else if (path.toString().endsWith("osc.bz2")) {
      return streamXmlChanges(new BZip2CompressorInputStream(newInputStream(path)),
          parallel);
    } else {
      throw new IOException("Unrecognized file extension: " + path.getFileName());
    }
  }

  public static Stream<Change> streamXmlChanges(InputStream input, boolean parallel) {
    Stream<Change> stream = StreamSupport.stream(new XmlChangeSpliterator(input), parallel);
    if (parallel) {
      stream = StreamUtils.batch(stream, 1000);
    }
    return stream;
  }

  public static State readState(Path path) throws IOException {
    return readState(newInputStream(path));
  }

  public static State readState(InputStream input) throws IOException {
    InputStreamReader reader = new InputStreamReader(input, Charsets.UTF_8);
    Map<String, String> map = new HashMap<>();
    for (String line : CharStreams.readLines(reader)) {
      String[] array = line.split("=");
      if (array.length == 2) {
        map.put(array[0], array[1]);
      }
    }
    long sequenceNumber = Long.parseLong(map.get("sequenceNumber"));
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    LocalDateTime timestamp = LocalDateTime.parse(map.get("timestamp").replace("\\", ""), format);
    return new State(sequenceNumber, timestamp);
  }

}
