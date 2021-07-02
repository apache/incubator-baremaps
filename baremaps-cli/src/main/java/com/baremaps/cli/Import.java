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
import com.baremaps.osm.cache.InMemoryCache;
import com.baremaps.osm.database.DatabaseImportService;
import com.baremaps.osm.database.HeaderTable;
import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.database.RelationTable;
import com.baremaps.osm.database.WayTable;
import com.baremaps.osm.lmdb.LmdbCoordinateCache;
import com.baremaps.osm.lmdb.LmdbReferencesCache;
import com.baremaps.osm.postgres.PostgresHeaderTable;
import com.baremaps.osm.postgres.PostgresNodeTable;
import com.baremaps.osm.postgres.PostgresRelationTable;
import com.baremaps.osm.postgres.PostgresWayTable;
import com.baremaps.postgres.jdbc.PostgresUtils;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
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
    LMDB, MEMORY
  }

  @Mixin
  private Options options;

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
  private final CacheType cacheType = CacheType.LMDB;

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
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());

    BlobStore blobStore = options.blobStore();
    DataSource datasource = PostgresUtils.datasource(database);
    HeaderTable headerTable = new PostgresHeaderTable(datasource);
    NodeTable nodeTable = new PostgresNodeTable(datasource);
    WayTable wayTable = new PostgresWayTable(datasource);
    RelationTable relationTable = new PostgresRelationTable(datasource);

    final Cache<Long, Coordinate> coordinateCache;
    final Cache<Long, List<Long>> referenceCache;
    switch (cacheType) {
      case MEMORY:
        coordinateCache = new InMemoryCache<>();
        referenceCache = new InMemoryCache<>();
        break;
      case LMDB:
        if (cacheDirectory != null) {
          cacheDirectory = Files.createDirectories(cacheDirectory);
        } else {
          cacheDirectory = Files.createTempDirectory("baremaps_");
        }
        Env<ByteBuffer> env = Env.create()
            .setMapSize(1_000_000_000_000L)
            .setMaxDbs(3)
            .open(cacheDirectory.toFile());
        coordinateCache = new LmdbCoordinateCache(env);
        referenceCache = new LmdbReferencesCache(env);
        break;
      default:
        throw new UnsupportedOperationException("Unsupported cache type");
    }

    new DatabaseImportService(
        file,
        blobStore,
        coordinateCache,
        referenceCache,
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        srid
    ).call();

    return 0;
  }

}
