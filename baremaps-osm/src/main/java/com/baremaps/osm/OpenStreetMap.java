/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.osm;

import static com.baremaps.stream.ConsumerUtils.consumeThenReturn;

import com.baremaps.osm.cache.CacheBlockConsumer;
import com.baremaps.osm.cache.CoordinateCache;
import com.baremaps.osm.cache.ReferenceCache;
import com.baremaps.osm.domain.Block;
import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.State;
import com.baremaps.osm.function.BlockEntityConsumer;
import com.baremaps.osm.geometry.CreateGeometryConsumer;
import com.baremaps.osm.geometry.ReprojectGeometryConsumer;
import com.baremaps.osm.pbf.BlobIterator;
import com.baremaps.osm.pbf.BlobUtils;
import com.baremaps.osm.xml.XmlChangeSpliterator;
import com.baremaps.osm.xml.XmlEntitySpliterator;
import com.baremaps.stream.StreamException;
import com.baremaps.stream.StreamUtils;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Utility methods for creating readers and streams from OpenStreetMap files. */
public class OpenStreetMap {

  private OpenStreetMap() {}

  private static Stream<Entity> streamPbfBlockEntities(Block block) {
    try {
      Stream.Builder<Entity> entities = Stream.builder();
      block.visit(new BlockEntityConsumer(entities::add));
      return entities.build();
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  /**
   * Creates an ordered stream of OSM blocks from a PBF file.
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
   * Creates an ordered stream of OSM blocks from a PBF file with geometries.
   *
   * @param input
   * @param coordinateCache
   * @param referenceCache
   * @param srid
   * @return
   */
  public static Stream<Block> streamPbfBlocksWithGeometries(
      InputStream input, CoordinateCache coordinateCache, ReferenceCache referenceCache, int srid) {
    Consumer<Block> cacheBlock = new CacheBlockConsumer(coordinateCache, referenceCache);
    Consumer<Entity> createGeometry = new CreateGeometryConsumer(coordinateCache, referenceCache);
    Consumer<Entity> reprojectGeometry = new ReprojectGeometryConsumer(4326, srid);
    Consumer<Block> prepareGeometries =
        new BlockEntityConsumer(createGeometry.andThen(reprojectGeometry));
    Function<Block, Block> prepareBlock = consumeThenReturn(cacheBlock.andThen(prepareGeometries));
    return streamPbfBlocks(input).map(prepareBlock);
  }

  /**
   * Creates an ordered stream of OSM entities from a PBF file.
   *
   * @param input
   * @return
   */
  public static Stream<Entity> streamPbfEntities(InputStream input) {
    return streamPbfBlocks(input).flatMap(OpenStreetMap::streamPbfBlockEntities);
  }

  /**
   * Creates an ordered stream of OSM entities from a PBF file with geometries.
   *
   * @param input
   * @param coordinateCache
   * @param referenceCache
   * @param srid
   * @return
   */
  public static Stream<Entity> streamPbfEntitiesWithGeometries(
      InputStream input, CoordinateCache coordinateCache, ReferenceCache referenceCache, int srid) {
    return streamPbfBlocksWithGeometries(input, coordinateCache, referenceCache, srid)
        .flatMap(OpenStreetMap::streamPbfBlockEntities);
  }

  /**
   * Creates an ordered stream of OSM entities from a XML file.
   *
   * @param input
   * @return
   */
  public static Stream<Entity> streamXmlEntities(InputStream input) {
    return StreamSupport.stream(new XmlEntitySpliterator(input), false);
  }

  /**
   * Creates an ordered stream of OSM changes from a XML file.
   *
   * @param input
   * @return
   */
  public static Stream<Change> streamXmlChanges(InputStream input) {
    return StreamSupport.stream(new XmlChangeSpliterator(input), false);
  }

  /**
   * Reads the content of an OSM state file.
   *
   * @param input
   * @return
   */
  public static State readState(InputStream input) throws IOException {
    InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
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
