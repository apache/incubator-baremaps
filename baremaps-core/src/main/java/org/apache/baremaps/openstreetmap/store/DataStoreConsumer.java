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

package org.apache.baremaps.openstreetmap.store;



import java.util.List;
import java.util.function.Consumer;
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.openstreetmap.model.Block;
import org.apache.baremaps.openstreetmap.model.DataBlock;
import org.apache.baremaps.stream.StreamException;
import org.locationtech.jts.geom.Coordinate;

/** A consumer that stores osm nodes and ways in the provided caches. */
public class DataStoreConsumer implements Consumer<Block> {

  private final LongDataMap<Coordinate> coordinates;
  private final LongDataMap<List<Long>> references;

  /**
   * Constructs a {@code CacheBlockConsumer} with the provided caches.
   *
   * @param coordinates the map of coordinates
   * @param references the map of references
   */
  public DataStoreConsumer(LongDataMap<Coordinate> coordinates,
      LongDataMap<List<Long>> references) {
    this.coordinates = coordinates;
    this.references = references;
  }

  /** {@inheritDoc} */
  @Override
  public void accept(Block block) {
    try {
      if (block instanceof DataBlock dataBlock) {
        dataBlock.denseNodes().stream()
            .forEach(node -> coordinates.put(node.id(), new Coordinate(node.lon(), node.lat())));
        dataBlock.nodes().stream()
            .forEach(node -> coordinates.put(node.id(), new Coordinate(node.lon(), node.lat())));
        dataBlock.ways().stream().forEach(way -> references.put(way.id(), way.nodes()));
      }
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }
}
