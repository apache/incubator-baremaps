/*
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

package org.apache.baremaps.openstreetmap.pbf;

import static org.apache.baremaps.stream.ConsumerUtils.consumeThenReturn;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;
import org.apache.baremaps.collection.DataMap;
import org.apache.baremaps.openstreetmap.function.*;
import org.apache.baremaps.openstreetmap.model.Block;
import org.apache.baremaps.stream.StreamUtils;
import org.locationtech.jts.geom.Coordinate;

/** A utility class for reading an OpenStreetMap pbf file. */
public class PbfBlockReader implements PbfReader<Block> {

  private int buffer = Runtime.getRuntime().availableProcessors();

  private boolean geometry = false;

  private int srid = 4326;

  private DataMap<Coordinate> coordinateMap;

  private DataMap<List<Long>> referenceMap;


  @Override
  public int buffer() {
    return buffer;
  }


  @Override
  public PbfBlockReader buffer(int buffer) {
    this.buffer = buffer;
    return this;
  }


  @Override
  public boolean geometries() {
    return geometry;
  }


  @Override
  public PbfBlockReader geometries(boolean geometries) {
    this.geometry = geometries;
    return this;
  }


  @Override
  public int projection() {
    return srid;
  }

  @Override
  public PbfBlockReader projection(int srid) {
    this.srid = srid;
    return this;
  }

  @Override
  public DataMap<Coordinate> coordinateMap() {
    return coordinateMap;
  }

  @Override
  public PbfBlockReader coordinateMap(DataMap<Coordinate> coordinateMap) {
    this.coordinateMap = coordinateMap;
    return this;
  }

  @Override
  public DataMap<List<Long>> referenceMap() {
    return referenceMap;
  }

  @Override
  public PbfBlockReader referenceMap(DataMap<List<Long>> referenceMap) {
    this.referenceMap = referenceMap;
    return this;
  }

  @Override
  public Stream<Block> stream(InputStream inputStream) {
    var blocks = StreamUtils.bufferInSourceOrder(
        StreamUtils.stream(new BlobIterator(inputStream)),
        new BlobToBlockMapper(),
        Runtime.getRuntime().availableProcessors());
    if (geometry) {
      // Initialize and chain the entity handlers
      var coordinateMapBuilder = new CoordinateMapBuilder(coordinateMap);
      var referenceMapBuilder = new ReferenceMapBuilder(referenceMap);
      var entityGeometryBuilder = new EntityGeometryBuilder(coordinateMap, referenceMap);
      var entityProjectionTransformer = new EntityProjectionTransformer(4326, srid);
      var entityHandler = coordinateMapBuilder
          .andThen(referenceMapBuilder)
          .andThen(entityGeometryBuilder)
          .andThen(entityProjectionTransformer);

      // Initialize the block mapper
      var blockMapper = consumeThenReturn(new BlockEntitiesHandler(entityHandler));

      blocks = blocks.map(blockMapper);
    }
    return blocks;
  }
}
