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

import com.baremaps.collection.AlignedDataList;
import com.baremaps.collection.LongDataSortedMap;
import com.baremaps.collection.LongSizedDataDenseMap;
import com.baremaps.collection.memory.OnDiskDirectoryMemory;
import com.baremaps.collection.type.LonLatDataType;
import com.baremaps.collection.type.LongDataType;
import com.baremaps.collection.type.LongListDataType;
import com.baremaps.collection.type.PairDataType;
import com.baremaps.collection.utils.FileUtils;
import com.baremaps.database.ImportService;
import com.baremaps.database.repository.PostgresHeaderRepository;
import com.baremaps.database.repository.PostgresNodeRepository;
import com.baremaps.database.repository.PostgresRelationRepository;
import com.baremaps.database.repository.PostgresWayRepository;
import com.baremaps.postgres.PostgresUtils;
import com.baremaps.workflow.Task;
import com.baremaps.workflow.WorkflowException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ImportOpenStreetMap(String file, String database, Integer databaseSrid)
    implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportOpenStreetMap.class);

  @Override
  public void run() {
    logger.info("Importing {} into {}", file, database);

    try (var dataSource = PostgresUtils.dataSource(database)) {
      var path = Paths.get(file).toAbsolutePath();

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
      var coordinatesDir = Files.createDirectories(cacheDir.resolve("coordinates"));
      var referencesKeysDir = Files.createDirectories(cacheDir.resolve("references_keys"));
      var referencesValuesDir = Files.createDirectories(cacheDir.resolve("references_vals"));

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

      new ImportService(
              path,
              coordinates,
              references,
              headerRepository,
              nodeRepository,
              wayRepository,
              relationRepository,
              databaseSrid)
          .call();

      FileUtils.deleteRecursively(cacheDir);

      logger.info("Finished importing {} into {}", file, database);
    } catch (Exception e) {
      logger.error("Failed importing {} into {}", file, database);
      throw new WorkflowException(e);
    }
  }
}
