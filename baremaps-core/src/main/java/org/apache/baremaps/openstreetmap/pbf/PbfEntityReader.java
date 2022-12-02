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
import java.util.stream.Stream;
import org.apache.baremaps.openstreetmap.OsmReader;
import org.apache.baremaps.openstreetmap.model.DataBlock;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.openstreetmap.model.HeaderBlock;
import org.apache.baremaps.stream.StreamException;

/** A utility class for flattening the blocks streamed by a {@link PbfBlockReader}. */
public class PbfEntityReader implements OsmReader<Entity> {

  private final PbfBlockReader reader;

  /**
   * Constructs an entity reader from a block reader.
   *
   * @param reader a block reader
   */
  public PbfEntityReader(PbfBlockReader reader) {
    this.reader = reader;
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
