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

import static org.apache.baremaps.stream.StreamUtils.batch;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.database.repository.HeaderRepository;
import org.apache.baremaps.database.repository.Repository;
import org.apache.baremaps.openstreetmap.function.*;
import org.apache.baremaps.openstreetmap.function.CacheMapper;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.openstreetmap.pbf.PbfBlockReader;
import org.apache.baremaps.utils.ProjectionTransformer;
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

  public ImportService(
      Path path, LongDataMap<Coordinate> coordinateMap,
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
    Function<Node, Node> nodeMapper = new NodeGeometryMapper(coordinateMap, referenceMap);
    Function<Way, Way> wayMapper = new WayGeometryMapper(coordinateMap, referenceMap);
    Function<Relation, Relation> relationMapper =
        new RelationGeometryMapper(coordinateMap, referenceMap);

    if (databaseSrid != 4326) {
      var projectionTransformer = new ProjectionTransformer(4326, databaseSrid);
      nodeMapper = nodeMapper.andThen(new ProjectionMapper<>(projectionTransformer));
      wayMapper = wayMapper.andThen(new ProjectionMapper<>(projectionTransformer));
      relationMapper = relationMapper.andThen(new ProjectionMapper<>(projectionTransformer));
    }

    var cacheBlock = new CacheMapper(coordinateMap, referenceMap);

    var prepareBlock = new BlockMapper(
        Function.identity(),
        new DataBlockMapper(nodeMapper, wayMapper, relationMapper));

    var saveBlock =
        new SaveBlockConsumer(headerRepository, nodeRepository, wayRepository, relationRepository);

    try (InputStream inputStream = Files.newInputStream(path)) {
      batch(new PbfBlockReader().stream(inputStream)
          .map(cacheBlock)
          .map(prepareBlock))
              .forEach(saveBlock);
    }
    return null;
  }
}
