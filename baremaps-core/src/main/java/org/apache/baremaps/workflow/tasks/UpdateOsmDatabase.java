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

public record UpdateOsmDatabase(Object database, Integer databaseSrid,
    String replicationUrl) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(UpdateOsmDatabase.class);

  public UpdateOsmDatabase(Object database, Integer databaseSrid) {
    this(database, databaseSrid, null);
  }

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

  public static void execute(DataMap<Long, Coordinate> coordinateMap,
      DataMap<Long, List<Long>> referenceMap,
      HeaderRepository headerRepository, Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository, Repository<Long, Relation> relationRepository,
      Integer databaseSrid,
      String replicationUrl) throws Exception {

    var header = headerRepository.selectLatest();

    // If the replicationUrl is not provided, use the one from the latest header.
    if (replicationUrl == null) {
      replicationUrl = header.getReplicationUrl();
    }

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

    var nextSequenceNumber = sequenceNumber + 1;
    var changeUrl = stateReader.getUrl(replicationUrl, nextSequenceNumber, "osc.gz");
    logger.info("Updating the database with the changeset: {}", changeUrl);

    var createGeometry = new EntityGeometryBuilder(coordinateMap, referenceMap);
    var reprojectGeometry = new EntityProjectionTransformer(4326, databaseSrid);
    var prepareGeometries = new ChangeEntitiesHandler(createGeometry.andThen(reprojectGeometry));
    var prepareChange = consumeThenReturn(prepareGeometries);
    var importChange = new PutChangeImporter(nodeRepository, wayRepository, relationRepository);

    try (var changeInputStream =
        new GZIPInputStream(new BufferedInputStream(changeUrl.openStream()))) {
      new XmlChangeReader().stream(changeInputStream).map(prepareChange).forEach(importChange);
    }

    var stateUrl = stateReader.getUrl(replicationUrl, nextSequenceNumber, "state.txt");
    try (var stateInputStream = new BufferedInputStream(stateUrl.openStream())) {
      var state = new StateReader().readState(stateInputStream);
      headerRepository.put(new Header(state.getSequenceNumber(), state.getTimestamp(),
          header.getReplicationUrl(), header.getSource(), header.getWritingProgram()));
    }
  }

}
