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
import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.CoordinateMapper;
import com.baremaps.osm.cache.LongListMapper;
import com.baremaps.osm.cache.LongMapper;
import com.baremaps.osm.cache.SimpleCache;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.lmdb.LmdbCache;
import com.baremaps.osm.postgres.PostgresHeaderRepository;
import com.baremaps.osm.postgres.PostgresNodeRepository;
import com.baremaps.osm.postgres.PostgresRelationRepository;
import com.baremaps.osm.postgres.PostgresWayRepository;
import com.baremaps.osm.repository.HeaderRepository;
import com.baremaps.osm.repository.ImportService;
import com.baremaps.osm.repository.Repository;
import com.baremaps.postgres.jdbc.PostgresUtils;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "import", description = "Import OpenStreetMap data in the database.")
public class Import implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Import.class);

  private enum CacheType {
    LMDB,
    MEMORY
  }

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
      names = {"--cache-type"},
      paramLabel = "CACHE_TYPE",
      description = "The type of cache used when importing data.")
  private CacheType cacheType = CacheType.LMDB;

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

    final Cache<Long, Coordinate> coordinateCache;
    final Cache<Long, List<Long>> referenceCache;
    switch (cacheType) {
      case MEMORY:
        coordinateCache = new SimpleCache<>();
        referenceCache = new SimpleCache<>();
        break;
      case LMDB:
        if (cacheDirectory != null) {
          cacheDirectory = Files.createDirectories(cacheDirectory);
        } else {
          cacheDirectory = Files.createTempDirectory("baremaps_");
        }
        Env<ByteBuffer> env =
            Env.create().setMapSize(1_000_000_000_000L).setMaxDbs(3).open(cacheDirectory.toFile());
        coordinateCache =
            new LmdbCache(
                env,
                env.openDbi("coordinate", DbiFlags.MDB_CREATE),
                new LongMapper(),
                new CoordinateMapper());
        referenceCache =
            new LmdbCache(
                env,
                env.openDbi("reference", DbiFlags.MDB_CREATE),
                new LongMapper(),
                new LongListMapper());
        break;
      default:
        throw new UnsupportedOperationException("Unsupported cache type");
    }

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
