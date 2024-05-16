/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.workflow.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.openstreetmap.pbf.PbfBlockReader;
import org.apache.baremaps.openstreetmap.postgres.PostgresHeaderRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresNodeRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresRelationRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresWayRepository;
import org.apache.baremaps.openstreetmap.repository.*;
import org.apache.baremaps.openstreetmap.repository.BlockImporter;
import org.apache.baremaps.openstreetmap.stream.StreamUtils;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import an OSM PBF file into a database.
 */
public class ImportOsmPbf implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportOsmPbf.class);

  private Path file;
  private Object database;
  private Integer databaseSrid;
  private Boolean replaceExisting;

  /**
   * Constructs a {@code ImportOsmPbf}.
   */
  public ImportOsmPbf() {

  }

  /**
   * Constructs an {@code ImportOsmPbf}.
   *
   * @param file the OSM PBF file
   * @param database the database
   * @param databaseSrid the database SRID
   * @param replaceExisting whether to replace the existing tables
   */
  public ImportOsmPbf(Path file, Object database,
      Integer databaseSrid, Boolean replaceExisting) {
    this.file = file;
    this.database = database;
    this.databaseSrid = databaseSrid;
    this.replaceExisting = replaceExisting;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(WorkflowContext context) throws Exception {
    var path = file.toAbsolutePath();

    // Initialize the repositories
    var datasource = context.getDataSource(database);
    var headerRepository = new PostgresHeaderRepository(datasource);
    var nodeRepository = new PostgresNodeRepository(datasource);
    var wayRepository = new PostgresWayRepository(datasource);
    var relationRepository = new PostgresRelationRepository(datasource);

    if (replaceExisting) {
      // Drop the existing tables
      headerRepository.drop();
      nodeRepository.drop();
      wayRepository.drop();
      relationRepository.drop();

      // Create the new tables
      headerRepository.create();
      nodeRepository.create();
      wayRepository.create();
      relationRepository.create();
    }

    var coordinateMap = context.getCoordinateMap();
    var referenceMap = context.getReferenceMap();

    execute(
        path,
        coordinateMap,
        referenceMap,
        headerRepository,
        nodeRepository,
        wayRepository,
        relationRepository,
        databaseSrid);
  }

  /**
   * Imports an OSM PBF file into a database.
   *
   * @param path the OSM PBF file
   * @param coordinateMap the coordinate map
   * @param referenceMap the reference map
   * @param headerRepository the header repository
   * @param nodeRepository the node repository
   * @param wayRepository the way repository
   * @param relationRepository the relation repository
   * @param databaseSrid the database SRID
   * @throws IOException
   */
  public static void execute(
      Path path,
      Map<Long, Coordinate> coordinateMap,
      Map<Long, List<Long>> referenceMap,
      HeaderRepository headerRepository,
      Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository,
      Integer databaseSrid) throws IOException {

    // configure the block reader
    var reader = new PbfBlockReader()
        .setGeometries(true)
        .setSrid(databaseSrid)
        .setCoordinateMap(coordinateMap)
        .setReferenceMap(referenceMap);

    // configure the block importer
    var importer = new BlockImporter(
        headerRepository,
        nodeRepository,
        wayRepository,
        relationRepository);

    // Stream and process the blocks
    try (var input = Files.newInputStream(path)) {
      StreamUtils.batch(reader.read(input)).forEach(importer);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new StringJoiner(", ", ImportOsmPbf.class.getSimpleName() + "[", "]")
        .add("file=" + file)
        .add("database=" + database)
        .add("databaseSrid=" + databaseSrid)
        .add("replaceExisting=" + replaceExisting)
        .toString();
  }
}
