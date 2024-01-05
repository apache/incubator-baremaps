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

import static org.apache.baremaps.stream.ConsumerUtils.consumeThenReturn;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import org.apache.baremaps.database.collection.DataMap;
import org.apache.baremaps.openstreetmap.function.ChangeEntitiesHandler;
import org.apache.baremaps.openstreetmap.function.EntityGeometryBuilder;
import org.apache.baremaps.openstreetmap.function.EntityProjectionTransformer;
import org.apache.baremaps.openstreetmap.model.Header;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.openstreetmap.postgres.PostgresCoordinateMap;
import org.apache.baremaps.openstreetmap.postgres.PostgresHeaderRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresNodeRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresReferenceMap;
import org.apache.baremaps.openstreetmap.postgres.PostgresRelationRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresWayRepository;
import org.apache.baremaps.openstreetmap.repository.*;
import org.apache.baremaps.openstreetmap.repository.PutChangeImporter;
import org.apache.baremaps.openstreetmap.state.StateReader;
import org.apache.baremaps.openstreetmap.xml.XmlChangeReader;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update an OSM database based on the header data stored in the database.
 */
public class UpdateOsmDatabase implements Task {

  private static final Logger logger = LoggerFactory.getLogger(UpdateOsmDatabase.class);

  private Object database;
  private Integer databaseSrid;
  private String replicationUrl;

  /**
   * Constructs a {@code UpdateOsmDatabase}.
   */
  public UpdateOsmDatabase() {

  }

  /**
   * Constructs an {@code UpdateOsmDatabase}.
   *
   * @param database the database
   * @param databaseSrid the database SRID
   */
  public UpdateOsmDatabase(Object database, Integer databaseSrid, String replicationUrl) {
    this.database = database;
    this.databaseSrid = databaseSrid;
    this.replicationUrl = replicationUrl;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(WorkflowContext context) throws Exception {
    var datasource = context.getDataSource(database);
    var coordinateMap = new PostgresCoordinateMap(datasource);
    var referenceMap = new PostgresReferenceMap(datasource);
    var headerRepository = new PostgresHeaderRepository(datasource);
    var nodeRepository = new PostgresNodeRepository(datasource);
    var wayRepository = new PostgresWayRepository(datasource);
    var relationRepository = new PostgresRelationRepository(datasource);
    execute(
        coordinateMap,
        referenceMap,
        headerRepository,
        nodeRepository,
        wayRepository,
        relationRepository,
        databaseSrid,
        replicationUrl);
  }

  /**
   * Executes the task.
   *
   * @param coordinateMap the coordinate map
   * @param referenceMap the reference map
   * @param headerRepository the header repository
   * @param nodeRepository the node repository
   * @param wayRepository the way repository
   * @param relationRepository the relation repository
   * @param databaseSrid the SRID
   * @throws Exception if something went wrong
   */
  public static void execute(DataMap<Long, Coordinate> coordinateMap,
      DataMap<Long, List<Long>> referenceMap,
      HeaderRepository headerRepository, Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository, Repository<Long, Relation> relationRepository,
      Integer databaseSrid,
      String replicationUrl) throws Exception {

    // Get the latest header from the database
    var header = headerRepository.selectLatest();

    // If the replicationUrl is not provided, use the one from the latest header.
    if (replicationUrl == null) {
      replicationUrl = header.getReplicationUrl();
    }

    // Get the sequence number of the latest header
    var stateReader = new StateReader(replicationUrl, true);
    var sequenceNumber = header.getReplicationSequenceNumber();

    // If the replicationTimestamp is not provided, guess it from the replication timestamp.
    if (sequenceNumber <= 0) {
      var replicationTimestamp = header.getReplicationTimestamp();
      var state = stateReader.getStateFromTimestamp(replicationTimestamp);
      if (state.isPresent()) {
        sequenceNumber = state.get().getSequenceNumber();
      }
    }

    // Increment the sequence number and get the changeset url
    var nextSequenceNumber = sequenceNumber + 1;
    var changeUrl = stateReader.getUrl(replicationUrl, nextSequenceNumber, "osc.gz");
    logger.info("Updating the database with the changeset: {}", changeUrl);

    // Process the changeset and update the database
    var createGeometry = new EntityGeometryBuilder(coordinateMap, referenceMap);
    var reprojectGeometry = new EntityProjectionTransformer(4326, databaseSrid);
    var prepareGeometries = new ChangeEntitiesHandler(createGeometry.andThen(reprojectGeometry));
    var prepareChange = consumeThenReturn(prepareGeometries);
    var importChange = new PutChangeImporter(nodeRepository, wayRepository, relationRepository);
    try (var changeInputStream =
        new GZIPInputStream(new BufferedInputStream(changeUrl.openStream()))) {
      new XmlChangeReader().stream(changeInputStream).map(prepareChange).forEach(importChange);
    }

    // Add the new header to the database
    var stateUrl = stateReader.getUrl(replicationUrl, nextSequenceNumber, "state.txt");
    try (var stateInputStream = new BufferedInputStream(stateUrl.openStream())) {
      var state = new StateReader().readState(stateInputStream);
      headerRepository.put(new Header(state.getSequenceNumber(), state.getTimestamp(),
          header.getReplicationUrl(), header.getSource(), header.getWritingProgram()));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new StringJoiner(", ", UpdateOsmDatabase.class.getSimpleName() + "[", "]")
        .add("database=" + database)
        .add("databaseSrid=" + databaseSrid)
        .add("replicationUrl='" + replicationUrl + "'")
        .toString();
  }
}
