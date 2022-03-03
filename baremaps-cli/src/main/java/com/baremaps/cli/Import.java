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

package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.postgres.PostgresHeaderRepository;
import com.baremaps.osm.postgres.PostgresNodeRepository;
import com.baremaps.osm.postgres.PostgresRelationRepository;
import com.baremaps.osm.postgres.PostgresWayRepository;
import com.baremaps.osm.repository.HeaderRepository;
import com.baremaps.osm.repository.ImportService;
import com.baremaps.osm.repository.Repository;
import com.baremaps.postgres.jdbc.PostgresUtils;
import com.baremaps.store.AlignedDataList;
import com.baremaps.store.DataStore;
import com.baremaps.store.LongAlignedDataDenseMap;
import com.baremaps.store.LongDataMap;
import com.baremaps.store.LongDataSortedMap;
import com.baremaps.store.memory.OnDiskMemory;
import com.baremaps.store.type.LonLatDataType;
import com.baremaps.store.type.LongDataType;
import com.baremaps.store.type.LongListDataType;
import com.baremaps.store.type.PairDataType;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "import", description = "Import OpenStreetMap data in the database.")
public class Import implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Import.class);

  @Mixin private Options options;

  @Option(
      names = {"--file"},
      paramLabel = "FILE",
      description = "The PBF file to import in the database.",
      required = true)
  private URI file;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the database.",
      required = true)
  private String database;

  @Option(
      names = {"--cache-directory"},
      paramLabel = "CACHE_DIRECTORY",
      description = "The directory used by the cache.")
  private Path cacheDirectory;

  @Option(
      names = {"--srid"},
      paramLabel = "SRID",
      description = "The projection used by the database.")
  private int srid = 3857;

  @Override
  public Integer call() throws Exception {
    BlobStore blobStore = options.blobStore();
    DataSource datasource = PostgresUtils.datasource(database);
    HeaderRepository headerRepository = new PostgresHeaderRepository(datasource);
    Repository<Long, Node> nodeRepository = new PostgresNodeRepository(datasource);
    Repository<Long, Way> wayRepository = new PostgresWayRepository(datasource);
    Repository<Long, Relation> relationRepository = new PostgresRelationRepository(datasource);

    Path directory = Paths.get(".");

    Path nodes = Files.createTempDirectory(directory, "nodes_");
    LongDataMap<Coordinate> coordinateCache =
        new LongAlignedDataDenseMap<>(new LonLatDataType(), new OnDiskMemory(nodes));

    Path referencesKeys = Files.createTempDirectory(directory, "ref_keys_");
    Path referencesValues = Files.createTempDirectory(directory, "ref_vals_");
    LongDataMap<List<Long>> referenceCache =
        new LongDataSortedMap<>(
            new AlignedDataList<>(
                new PairDataType<>(new LongDataType(), new LongDataType()),
                new OnDiskMemory(referencesKeys)),
            new DataStore<>(new LongListDataType(), new OnDiskMemory(referencesValues)));

    logger.info("Importing data");
    new ImportService(
            file,
            blobStore,
            coordinateCache,
            referenceCache,
            headerRepository,
            nodeRepository,
            wayRepository,
            relationRepository,
            srid)
        .call();

    logger.info("Done");

    return 0;
  }
}
