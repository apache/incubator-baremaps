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

package org.apache.baremaps.database;

import static org.apache.baremaps.stream.ConsumerUtils.consumeThenReturn;
import static org.apache.baremaps.stream.StreamUtils.batch;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.database.repository.HeaderRepository;
import org.apache.baremaps.database.repository.Repository;
import org.apache.baremaps.openstreetmap.function.BlockEntityConsumer;
import org.apache.baremaps.openstreetmap.function.CreateGeometryConsumer;
import org.apache.baremaps.openstreetmap.function.ReprojectEntityConsumer;
import org.apache.baremaps.openstreetmap.model.Block;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.openstreetmap.pbf.PbfBlockReader;
import org.apache.baremaps.openstreetmap.store.DataStoreConsumer;
import org.locationtech.jts.geom.Coordinate;

public class ImportService implements Callable<Void> {

  private final Path path;
  private final LongDataMap<Coordinate> coordinates;
  private final LongDataMap<List<Long>> references;
  private final HeaderRepository headerRepository;
  private final Repository<Long, Node> nodeRepository;
  private final Repository<Long, Way> wayRepository;
  private final Repository<Long, Relation> relationRepository;
  private final int databaseSrid;

  public ImportService(Path path, LongDataMap<Coordinate> coordinates,
      LongDataMap<List<Long>> references, HeaderRepository headerRepository,
      Repository<Long, Node> nodeRepository, Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository, Integer databaseSrid) {
    this.path = path;
    this.coordinates = coordinates;
    this.references = references;
    this.headerRepository = headerRepository;
    this.nodeRepository = nodeRepository;
    this.wayRepository = wayRepository;
    this.relationRepository = relationRepository;
    this.databaseSrid = databaseSrid;
  }

  @Override
  public Void call() throws Exception {
    Consumer<Block> cacheBlock = new DataStoreConsumer(coordinates, references);
    Consumer<Entity> createGeometry = new CreateGeometryConsumer(coordinates, references);
    Consumer<Entity> reprojectGeometry = new ReprojectEntityConsumer(4326, databaseSrid);
    Consumer<Block> prepareGeometries =
        new BlockEntityConsumer(createGeometry.andThen(reprojectGeometry));
    Function<Block, Block> prepareBlock = consumeThenReturn(cacheBlock.andThen(prepareGeometries));
    Consumer<Block> saveBlock =
        new SaveBlockConsumer(headerRepository, nodeRepository, wayRepository, relationRepository);
    try (InputStream inputStream = Files.newInputStream(path)) {
      batch(new PbfBlockReader().stream(inputStream).map(prepareBlock)).forEach(saveBlock);
    }
    return null;
  }
}
