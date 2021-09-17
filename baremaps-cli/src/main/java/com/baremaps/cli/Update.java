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
import com.baremaps.osm.cache.CoordinateCache;
import com.baremaps.osm.cache.ReferenceCache;
import com.baremaps.osm.database.HeaderTable;
import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.database.RelationTable;
import com.baremaps.osm.database.UpdateService;
import com.baremaps.osm.database.WayTable;
import com.baremaps.osm.postgres.PostgresCoordinateCache;
import com.baremaps.osm.postgres.PostgresHeaderTable;
import com.baremaps.osm.postgres.PostgresNodeTable;
import com.baremaps.osm.postgres.PostgresReferenceCache;
import com.baremaps.osm.postgres.PostgresRelationTable;
import com.baremaps.osm.postgres.PostgresWayTable;
import com.baremaps.postgres.jdbc.PostgresUtils;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "update", description = "Update OpenStreetMap data in the database (experimental).")
public class Update implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Update.class);

  @Mixin private Options options;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the database.",
      required = true)
  private String database;

  @Option(
      names = {"--srid"},
      paramLabel = "SRID",
      description = "The projection used by the database.")
  private int srid = 3857;

  @Override
  public Integer call() throws Exception {
    BlobStore blobStore = options.blobStore();
    DataSource datasource = PostgresUtils.datasource(database);
    CoordinateCache coordinateCache = new PostgresCoordinateCache(datasource);
    ReferenceCache referenceCache = new PostgresReferenceCache(datasource);
    HeaderTable headerTable = new PostgresHeaderTable(datasource);
    NodeTable nodeTable = new PostgresNodeTable(datasource);
    WayTable wayTable = new PostgresWayTable(datasource);
    RelationTable relationTable = new PostgresRelationTable(datasource);

    logger.info("Importing changes");
    new UpdateService(
            blobStore,
            coordinateCache,
            referenceCache,
            headerTable,
            nodeTable,
            wayTable,
            relationTable,
            srid)
        .call();
    logger.info("Done");

    return 0;
  }
}
