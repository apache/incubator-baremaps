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

import org.apache.baremaps.collection.AlignedDataList;
import org.apache.baremaps.collection.LongDataSortedMap;
import org.apache.baremaps.collection.LongSizedDataDenseMap;
import org.apache.baremaps.collection.DataStore;
import org.apache.baremaps.collection.memory.OnDiskDirectoryMemory;
import org.apache.baremaps.collection.type.LonLatDataType;
import org.apache.baremaps.collection.type.LongDataType;
import org.apache.baremaps.collection.type.LongListDataType;
import org.apache.baremaps.collection.type.PairDataType;
import org.apache.baremaps.collection.utils.FileUtils;
import org.apache.baremaps.database.ImportService;
import org.apache.baremaps.database.repository.PostgresHeaderRepository;
import org.apache.baremaps.database.repository.PostgresNodeRepository;
import org.apache.baremaps.database.repository.PostgresRelationRepository;
import org.apache.baremaps.database.repository.PostgresWayRepository;
import org.apache.baremaps.postgres.PostgresUtils;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowException;
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
          new DataStore<>(
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
