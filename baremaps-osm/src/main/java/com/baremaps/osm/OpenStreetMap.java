package com.baremaps.osm;

import com.baremaps.osm.domain.Block;
import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.State;
import com.baremaps.osm.handler.BlockEntityHandler;
import com.baremaps.osm.pbf.BlobIterator;
import com.baremaps.osm.pbf.BlobUtils;
import com.baremaps.osm.xml.XmlChangeSpliterator;
import com.baremaps.osm.xml.XmlEntitySpliterator;
import com.baremaps.stream.StreamException;
import com.baremaps.stream.StreamUtils;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility methods for creating readers and streams from OpenStreetMap files.
 */
public class OpenStreetMap {

  private OpenStreetMap() {

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

  /**
   * Create an ordered stream of OSM blocks from a PBF file.
   *
   * @param input
   * @return
   */
  public static Stream<Block> streamPbfBlocks(InputStream input) {
    return StreamUtils.bufferInSourceOrder(
        StreamUtils.stream(new BlobIterator(input)),
        BlobUtils::readBlock,
        Runtime.getRuntime().availableProcessors());
  }

  /**
   * Create an ordered stream of OSM entities from a PBF file.
   *
   * @param input
   * @return
   */
  public static Stream<Entity> streamPbfEntities(InputStream input) {
    return streamPbfBlocks(input).flatMap(OpenStreetMap::streamPbfBlockEntities);
  }

  /**
   * Create an ordered stream of OSM entities from a XML file.
   *
   * @param input
   * @return
   */
  public static Stream<Entity> streamXmlEntities(InputStream input) {
    return StreamSupport.stream(new XmlEntitySpliterator(input), false);
  }

  /**
   * Create an ordered stream of OSM changes from a XML file.
   *
   * @param input
   * @return
   */
  public static Stream<Change> streamXmlChanges(InputStream input) {
    return StreamSupport.stream(new XmlChangeSpliterator(input), false);
  }

  /**
   * Read the content of an OSM state file.
   *
   * @param input
   * @return
   */
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
