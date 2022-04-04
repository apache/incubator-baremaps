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

package com.baremaps.cli.pipeline;

import com.baremaps.cli.Options;
import com.baremaps.collection.AlignedDataList;
import com.baremaps.collection.DataStore;
import com.baremaps.collection.LongDataMap;
import com.baremaps.collection.LongDataSortedMap;
import com.baremaps.collection.LongSizedDataDenseMap;
import com.baremaps.collection.memory.OnDiskDirectoryMemory;
import com.baremaps.collection.type.LonLatDataType;
import com.baremaps.collection.type.LongDataType;
import com.baremaps.collection.type.LongListDataType;
import com.baremaps.collection.type.PairDataType;
import com.baremaps.collection.utils.FileUtils;
import com.baremaps.core.Context;
import com.baremaps.core.blob.Blob;
import com.baremaps.core.blob.BlobStore;
import com.baremaps.core.blob.BlobStoreRouter;
import com.baremaps.core.config.Config;
import com.baremaps.core.database.ImportService;
import com.baremaps.core.database.repository.HeaderRepository;
import com.baremaps.core.database.repository.PostgresHeaderRepository;
import com.baremaps.core.database.repository.PostgresNodeRepository;
import com.baremaps.core.database.repository.PostgresRelationRepository;
import com.baremaps.core.database.repository.PostgresWayRepository;
import com.baremaps.core.database.repository.Repository;
import com.baremaps.core.postgres.PostgresUtils;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Pipe;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "pipeline", description = "Execute a pipeline.")
public class Pipeline implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);

  @Mixin private Options options;

  @Option(
      names = {"--config"},
      paramLabel = "CONFIG",
      description = "The pipeline configuration.",
      required = true)
  private URI config;

  @Override
  public Integer call() throws Exception {
    BlobStore blobStore = options.blobStore();
    Blob blob = blobStore.get(config);
    ObjectMapper mapper = new ObjectMapper();
    Config config = mapper.readValue(blob.getInputStream(), Config.class);
    DataSource dataSource = PostgresUtils.datasource(config.getDatabase());
    Path directory = Files.createDirectories(Paths.get("pipeline"));
    Context context =
        new Context() {
          @Override
          public Path directory() {
            return directory;
          }

          @Override
          public BlobStore blobStore() {
            return blobStore;
          }

          @Override
          public DataSource dataSource() {
            return dataSource;
          }
        };
    com.baremaps.core.Pipeline pipeline = new com.baremaps.core.Pipeline(context, config);
    pipeline.execute();
    return 0;
  }
}
