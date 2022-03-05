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

package com.baremaps.osm.pbf;

import static com.baremaps.stream.ConsumerUtils.consumeThenReturn;

import com.baremaps.osm.domain.Block;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.function.BlockEntityConsumer;
import com.baremaps.osm.geometry.CreateGeometryConsumer;
import com.baremaps.osm.geometry.ReprojectEntityConsumer;
import com.baremaps.osm.store.DataStoreConsumer;
import com.baremaps.store.LongDataMap;
import com.baremaps.stream.StreamException;
import com.baremaps.stream.StreamUtils;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.locationtech.jts.geom.Coordinate;

public class OsmPbfParser {

  private int buffer = Runtime.getRuntime().availableProcessors();

  private boolean geometry = false;

  private int srid = 4326;

  private LongDataMap<Coordinate> coordinateMap;

  private LongDataMap<List<Long>> referenceMap;

  public int buffer() {
    return buffer;
  }

  public OsmPbfParser buffer(int buffer) {
    this.buffer = buffer;
    return this;
  }

  public boolean geometry() {
    return geometry;
  }

  public OsmPbfParser geometry(boolean geometries) {
    this.geometry = geometries;
    return this;
  }

  public int projection() {
    return srid;
  }

  public OsmPbfParser projection(int srid) {
    this.srid = srid;
    return this;
  }

  public LongDataMap<Coordinate> coordinateMap() {
    return coordinateMap;
  }

  public OsmPbfParser coordinateMap(LongDataMap<Coordinate> coordinateMap) {
    this.coordinateMap = coordinateMap;
    return this;
  }

  public LongDataMap<List<Long>> referenceMap() {
    return referenceMap;
  }

  public OsmPbfParser referenceMap(LongDataMap<List<Long>> referenceMap) {
    this.referenceMap = referenceMap;
    return this;
  }

  /**
   * Creates an ordered stream of blocks.
   *
   * @param inputStream an osm pbf {@link InputStream}
   * @return a stream of blocks
   */
  public Stream<Block> blocks(InputStream inputStream) {
    Stream<Block> blocks =
        StreamUtils.bufferInSourceOrder(
            StreamUtils.stream(new BlobIterator(inputStream)),
            BlobUtils::readBlock,
            Runtime.getRuntime().availableProcessors());
    if (geometry) {
      Consumer<Block> cacheBlock = new DataStoreConsumer(coordinateMap, referenceMap);
      Consumer<Entity> createGeometry = new CreateGeometryConsumer(coordinateMap, referenceMap);
      if (srid != 4326) {
        Consumer<Entity> reprojectGeometry = new ReprojectEntityConsumer(4326, srid);
        createGeometry = createGeometry.andThen(reprojectGeometry);
      }
      Consumer<Block> prepareGeometries = new BlockEntityConsumer(createGeometry);
      Function<Block, Block> prepareBlock =
          consumeThenReturn(cacheBlock.andThen(prepareGeometries));
      blocks = blocks.map(prepareBlock);
    }
    return blocks;
  }

  /**
   * Creates an ordered stream of entities.
   *
   * @param inputStream an osm pbf {@link InputStream}
   * @return a stream of blocks
   */
  public Stream<Entity> entities(InputStream inputStream) {
    return blocks(inputStream)
        .flatMap(
            block -> {
              try {
                Stream.Builder<Entity> entities = Stream.builder();
                block.visit(new BlockEntityConsumer(entities::add));
                return entities.build();
              } catch (Exception e) {
                throw new StreamException(e);
              }
            });
  }
}
