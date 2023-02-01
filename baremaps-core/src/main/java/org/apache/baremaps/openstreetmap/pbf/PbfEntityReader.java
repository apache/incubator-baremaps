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



import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;
import org.apache.baremaps.collection.DataMap;
import org.apache.baremaps.openstreetmap.model.DataBlock;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.openstreetmap.model.HeaderBlock;
import org.apache.baremaps.stream.StreamException;
import org.locationtech.jts.geom.Coordinate;

/** A utility class for flattening the blocks streamed by a {@link PbfBlockReader}. */
public class PbfEntityReader implements PbfReader<Entity> {

  private final PbfBlockReader reader;

  /**
   * Constructs an entity reader from a block reader.
   */
  public PbfEntityReader() {
    this.reader = new PbfBlockReader();
  }

  @Override
  public int buffer() {
    return reader.buffer();
  }

  @Override
  public PbfEntityReader buffer(int buffer) {
    reader.buffer(buffer);
    return this;
  }

  @Override
  public boolean geometries() {
    return reader.geometries();
  }

  @Override
  public PbfEntityReader geometries(boolean geometries) {
    reader.geometries(geometries);
    return this;
  }

  @Override
  public int projection() {
    return reader.projection();
  }

  @Override
  public PbfEntityReader projection(int srid) {
    reader.projection(srid);
    return this;
  }

  @Override
  public DataMap<Coordinate> coordinateMap() {
    return reader.coordinateMap();
  }

  @Override
  public PbfEntityReader coordinateMap(DataMap<Coordinate> coordinateMap) {
    reader.coordinateMap(coordinateMap);
    return this;
  }

  @Override
  public DataMap<List<Long>> referenceMap() {
    return reader.referenceMap();
  }

  @Override
  public PbfEntityReader referenceMap(DataMap<List<Long>> referenceMap) {
    reader.referenceMap(referenceMap);
    return this;
  }

  /**
   * Creates an ordered stream of entities.
   *
   * @param inputStream an osm pbf {@link InputStream}
   * @return a stream of blocks
   */
  public Stream<Entity> stream(InputStream inputStream) {
    return reader.stream(inputStream).flatMap(block -> {
      try {
        Stream.Builder<Entity> entities = Stream.builder();
        if (block instanceof HeaderBlock headerBlock) {
          entities.add(headerBlock.getHeader());
          entities.add(headerBlock.getBound());
        } else if (block instanceof DataBlock dataBlock) {
          dataBlock.getDenseNodes().forEach(entities::add);
          dataBlock.getNodes().forEach(entities::add);
          dataBlock.getWays().forEach(entities::add);
          dataBlock.getRelations().forEach(entities::add);
        } else {
          throw new StreamException("Unknown block type.");
        }
        return entities.build();
      } catch (Exception e) {
        throw new StreamException(e);
      }
    });
  }
}
