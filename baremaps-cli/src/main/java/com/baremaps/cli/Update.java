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
import com.baremaps.osm.postgres.PostgresCoordinateCache;
import com.baremaps.osm.postgres.PostgresHeaderRepository;
import com.baremaps.osm.postgres.PostgresNodeRepository;
import com.baremaps.osm.postgres.PostgresReferenceCache;
import com.baremaps.osm.postgres.PostgresRelationRepository;
import com.baremaps.osm.postgres.PostgresWayRepository;
import com.baremaps.osm.repository.HeaderRepository;
import com.baremaps.osm.repository.Repository;
import com.baremaps.osm.repository.UpdateService;
import com.baremaps.postgres.jdbc.PostgresUtils;
import com.baremaps.store.LongDataMap;
import java.util.List;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Coordinate;
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
    LongDataMap<Coordinate> coordinateCache = new PostgresCoordinateCache(datasource);
    LongDataMap<List<Long>> referenceCache = new PostgresReferenceCache(datasource);
    HeaderRepository headerRepository = new PostgresHeaderRepository(datasource);
    Repository<Long, Node> nodeRepository = new PostgresNodeRepository(datasource);
    Repository<Long, Way> wayRepository = new PostgresWayRepository(datasource);
    Repository<Long, Relation> relationRepository = new PostgresRelationRepository(datasource);

    logger.info("Importing changes");
    new UpdateService(
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
