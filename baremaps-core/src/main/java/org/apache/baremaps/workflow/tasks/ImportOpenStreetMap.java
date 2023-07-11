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

package org.apache.baremaps.workflow.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.baremaps.database.collection.*;
import org.apache.baremaps.database.memory.MemoryMappedDirectory;
import org.apache.baremaps.database.type.LongDataType;
import org.apache.baremaps.database.type.LongListDataType;
import org.apache.baremaps.database.type.PairDataType;
import org.apache.baremaps.database.type.geometry.LonLatDataType;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.openstreetmap.pbf.PbfBlockReader;
import org.apache.baremaps.openstreetmap.postgres.PostgresHeaderRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresNodeRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresRelationRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresWayRepository;
import org.apache.baremaps.openstreetmap.repository.*;
import org.apache.baremaps.openstreetmap.repository.BlockImporter;
import org.apache.baremaps.stream.StreamUtils;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ImportOpenStreetMap(Path file, String database, Integer databaseSrid)
    implements
      Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportOpenStreetMap.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var dataSource = context.getDataSource(database);
    var path = file.toAbsolutePath();

    var headerRepository = new PostgresHeaderRepository(dataSource);
    var nodeRepository = new PostgresNodeRepository(dataSource);
    var wayRepository = new PostgresWayRepository(dataSource);
    var relationRepository = new PostgresRelationRepository(dataSource);

    headerRepository.drop();
    nodeRepository.drop();
    wayRepository.drop();
    relationRepository.drop();

    headerRepository.create();
    nodeRepository.create();
    wayRepository.create();
    relationRepository.create();

    var cacheDir = Files.createTempDirectory(Paths.get("."), "cache_");

    var coordinatesKeysFile = Files.createDirectory(cacheDir.resolve("coordinates_keys"));
    var coordinatesValsFile = Files.createDirectory(cacheDir.resolve("coordinates_vals"));
    var coordinateMap =
        new Long2ObjectOpenHashDataMap<>(100_000_000L, 0.9f,
            () -> new Long2ObjectMemoryAlignedDataMap<>(
                new LongDataType(),
                new MemoryMappedDirectory(coordinatesKeysFile)),
            () -> new Long2ObjectMemoryAlignedDataMap<>(
                new LonLatDataType(),
                new MemoryMappedDirectory(coordinatesValsFile)));

    var referencesKeysDir = Files.createDirectory(cacheDir.resolve("references_keys"));
    var referencesValuesDir = Files.createDirectory(cacheDir.resolve("references_vals"));

    var referenceMap =
        new Long2ObjectIndexedDataMap<>(
            new Long2LongPackedOpenHashDataMap(
                10_000_000L,
                0.9f,
                () -> new Long2ObjectMemoryAlignedDataMap<>(
                    new PairDataType<>(new LongDataType(), new LongDataType()),
                    new MemoryMappedDirectory(referencesKeysDir))),
            new AppendOnlyBuffer<>(
                new LongListDataType(),
                new MemoryMappedDirectory(referencesValuesDir)));

    // var referenceMap =
    // new MonotonicDataMap<>(
    // new MemoryAlignedDataList<>(
    // new PairDataType<>(new LongDataType(), new LongDataType()),
    // new MemoryMappedDirectory(referencesKeysDir)),
    // new AppendOnlyBuffer<>(
    // new LongListDataType(),
    // new MemoryMappedDirectory(referencesValuesDir)));

    execute(
        path,
        coordinateMap,
        referenceMap,
        headerRepository,
        nodeRepository,
        wayRepository,
        relationRepository,
        databaseSrid);

    // FileUtils.deleteRecursively(cacheDir);
  }

  public static void execute(
      Path path,
      DataMap<Coordinate> coordinateMap,
      DataMap<List<Long>> referenceMap,
      HeaderRepository headerRepository,
      Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository,
      Integer databaseSrid) throws IOException {

    // configure the block reader
    var reader = new PbfBlockReader()
        .geometries(true)
        .projection(databaseSrid)
        .coordinateMap(coordinateMap)
        .referenceMap(referenceMap);

    // configure the block importer
    var importer = new BlockImporter(
        headerRepository,
        nodeRepository,
        wayRepository,
        relationRepository);

    // Stream and process the blocks
    try (var input = Files.newInputStream(path)) {
      StreamUtils.batch(reader.stream(input)).forEach(importer);
    }
  }
}
