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
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.database.repository.HeaderRepository;
import org.apache.baremaps.database.repository.Repository;
import org.apache.baremaps.openstreetmap.function.BlockEntitiesHandler;
import org.apache.baremaps.openstreetmap.function.CacheBuilder;
import org.apache.baremaps.openstreetmap.function.EntityGeometryBuilder;
import org.apache.baremaps.openstreetmap.function.EntityProjectionTransformer;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.openstreetmap.pbf.PbfBlockReader;
import org.locationtech.jts.geom.Coordinate;

public class ImportService implements Callable<Void> {

  private final Path path;
  private final LongDataMap<Coordinate> coordinateMap;
  private final LongDataMap<List<Long>> referenceMap;
  private final HeaderRepository headerRepository;
  private final Repository<Long, Node> nodeRepository;
  private final Repository<Long, Way> wayRepository;
  private final Repository<Long, Relation> relationRepository;
  private final int databaseSrid;

  public ImportService(Path path, LongDataMap<Coordinate> coordinateMap,
      LongDataMap<List<Long>> referenceMap, HeaderRepository headerRepository,
      Repository<Long, Node> nodeRepository, Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository, Integer databaseSrid) {
    this.path = path;
    this.coordinateMap = coordinateMap;
    this.referenceMap = referenceMap;
    this.headerRepository = headerRepository;
    this.nodeRepository = nodeRepository;
    this.wayRepository = wayRepository;
    this.relationRepository = relationRepository;
    this.databaseSrid = databaseSrid;
  }

  @Override
  public Void call() throws Exception {
    var cacheBuilder = new CacheBuilder(coordinateMap, referenceMap);
    var entityGeometryBuilder = new EntityGeometryBuilder(coordinateMap, referenceMap);
    var entityProjectionTransformer = new EntityProjectionTransformer(4326, databaseSrid);
    var blockEntitiesHandler =
        new BlockEntitiesHandler(entityGeometryBuilder.andThen(entityProjectionTransformer));
    var blockMapper = consumeThenReturn(cacheBuilder.andThen(blockEntitiesHandler));
    var blockImporter =
        new BlockImporter(headerRepository, nodeRepository, wayRepository, relationRepository);
    try (InputStream inputStream = Files.newInputStream(path)) {
      batch(new PbfBlockReader().stream(inputStream).map(blockMapper)).forEach(blockImporter);
    }
    return null;
  }

}
