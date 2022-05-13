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

package com.baremaps.workflow.tasks;

import static com.baremaps.stream.ConsumerUtils.consumeThenReturn;
import static com.baremaps.stream.StreamUtils.batch;

import com.baremaps.collection.AlignedDataList;
import com.baremaps.collection.LongDataSortedMap;
import com.baremaps.collection.LongSizedDataDenseMap;
import com.baremaps.collection.memory.OnDiskDirectoryMemory;
import com.baremaps.collection.type.LonLatDataType;
import com.baremaps.collection.type.LongDataType;
import com.baremaps.collection.type.LongListDataType;
import com.baremaps.collection.type.PairDataType;
import com.baremaps.collection.utils.FileUtils;
import com.baremaps.database.SaveBlockConsumer;
import com.baremaps.database.repository.PostgresHeaderRepository;
import com.baremaps.database.repository.PostgresNodeRepository;
import com.baremaps.database.repository.PostgresRelationRepository;
import com.baremaps.database.repository.PostgresWayRepository;
import com.baremaps.osm.domain.Block;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.function.BlockEntityConsumer;
import com.baremaps.osm.function.CreateGeometryConsumer;
import com.baremaps.osm.function.ReprojectEntityConsumer;
import com.baremaps.osm.pbf.PbfBlockReader;
import com.baremaps.osm.store.DataStoreConsumer;
import com.baremaps.workflow.Task;
import com.baremaps.workflow.WorkflowException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public record ImportOsmPbf(
    String id,
    List<String> needs,
    String file,
    Database database,
    Integer sourceSRID,
    Integer targetSRID)
    implements Task {

  @Override
  public void run() {
    try {
      var cacheDir = Files.createTempDirectory(Paths.get("."), "cache_");
      var path = Paths.get(file);
      var coordinatesDir = Files.createDirectories(cacheDir.resolve("coordinates"));
      var referencesKeysDir = Files.createDirectories(cacheDir.resolve("references_keys"));
      var referencesValuesDir = Files.createDirectories(cacheDir.resolve("references_values"));

      var url =
          String.format(
              "jdbc:postgresql://%s:%s/%s?&user=%s&password=%s",
              database.host(),
              database.port(),
              database.name(),
              database.username(),
              database.password());

      var config = new HikariConfig();
      config.setPoolName("BaremapsDataSource");
      config.setJdbcUrl(url);
      config.setMaximumPoolSize(Runtime.getRuntime().availableProcessors());

      try (var dataSource = new HikariDataSource(config)) {
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

        var coordinates =
            new LongSizedDataDenseMap<>(
                new LonLatDataType(), new OnDiskDirectoryMemory(coordinatesDir));
        var references =
            new LongDataSortedMap<>(
                new AlignedDataList<>(
                    new PairDataType<>(new LongDataType(), new LongDataType()),
                    new OnDiskDirectoryMemory(referencesKeysDir)),
                new com.baremaps.collection.DataStore<>(
                    new LongListDataType(), new OnDiskDirectoryMemory(referencesValuesDir)));

        Consumer<Block> cacheBlock = new DataStoreConsumer(coordinates, references);
        Consumer<Entity> createGeometry = new CreateGeometryConsumer(coordinates, references);
        Consumer<Entity> reprojectGeometry = new ReprojectEntityConsumer(sourceSRID, targetSRID);
        Consumer<Block> prepareGeometries =
            new BlockEntityConsumer(createGeometry.andThen(reprojectGeometry));
        Function<Block, Block> prepareBlock =
            consumeThenReturn(cacheBlock.andThen(prepareGeometries));
        Consumer<Block> saveBlock =
            new SaveBlockConsumer(
                headerRepository, nodeRepository, wayRepository, relationRepository);
        try (InputStream inputStream = Files.newInputStream(path)) {
          batch(new PbfBlockReader().stream(inputStream).map(prepareBlock)).forEach(saveBlock);
        }

        FileUtils.deleteRecursively(cacheDir);
      }
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }
}
