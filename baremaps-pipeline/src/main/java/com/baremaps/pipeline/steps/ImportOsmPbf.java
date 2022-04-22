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

package com.baremaps.pipeline.steps;

import com.baremaps.collection.AlignedDataList;
import com.baremaps.collection.LongDataSortedMap;
import com.baremaps.collection.LongSizedDataDenseMap;
import com.baremaps.collection.memory.OnDiskDirectoryMemory;
import com.baremaps.collection.type.LonLatDataType;
import com.baremaps.collection.type.LongDataType;
import com.baremaps.collection.type.LongListDataType;
import com.baremaps.collection.type.PairDataType;
import com.baremaps.collection.utils.FileUtils;
import com.baremaps.pipeline.Context;
import com.baremaps.pipeline.PipelineException;
import com.baremaps.pipeline.Step;
import com.baremaps.pipeline.database.ImportService;
import com.baremaps.pipeline.database.repository.PostgresHeaderRepository;
import com.baremaps.pipeline.database.repository.PostgresNodeRepository;
import com.baremaps.pipeline.database.repository.PostgresRelationRepository;
import com.baremaps.pipeline.database.repository.PostgresWayRepository;
import java.nio.file.Files;
import java.util.List;

public record ImportOsmPbf(
    String id, List<String> needs, String file, Integer sourceSRID, Integer targetSRID)
    implements Step {

  @Override
  public void execute(Context context) {
    try {
      var cacheDir = Files.createTempDirectory(context.directory(), "cache_");
      var sourceFile = context.directory().resolve(file);
      var coordinatesDir = Files.createDirectories(cacheDir.resolve("coordinates"));
      var referencesKeysDir = Files.createDirectories(cacheDir.resolve("references_keys"));
      var referencesValuesDir = Files.createDirectories(cacheDir.resolve("references_values"));

      var dataSource = context.dataSource();
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

      new ImportService(
              sourceFile.toUri(),
              context.blobStore(),
              coordinates,
              references,
              headerRepository,
              nodeRepository,
              wayRepository,
              relationRepository,
              sourceSRID,
              targetSRID)
          .call();

      FileUtils.deleteRecursively(cacheDir);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }
}
