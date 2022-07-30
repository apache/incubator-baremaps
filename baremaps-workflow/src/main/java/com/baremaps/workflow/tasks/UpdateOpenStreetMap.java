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

package com.baremaps.workflow.tasks;

import com.baremaps.collection.LongDataMap;
import com.baremaps.database.UpdateService;
import com.baremaps.database.collection.PostgresCoordinateMap;
import com.baremaps.database.collection.PostgresReferenceMap;
import com.baremaps.database.repository.HeaderRepository;
import com.baremaps.database.repository.PostgresHeaderRepository;
import com.baremaps.database.repository.PostgresNodeRepository;
import com.baremaps.database.repository.PostgresRelationRepository;
import com.baremaps.database.repository.PostgresWayRepository;
import com.baremaps.database.repository.Repository;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.postgres.PostgresUtils;
import com.baremaps.workflow.Task;
import com.baremaps.workflow.WorkflowException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record UpdateOpenStreetMap(String database, Integer databaseSrid) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(UpdateOpenStreetMap.class);

  @Override
  public void run() {
    logger.info("Updating {}", database);
    try {
      DataSource datasource = PostgresUtils.dataSource(database);
      LongDataMap<Coordinate> coordinates = new PostgresCoordinateMap(datasource);
      LongDataMap<List<Long>> references = new PostgresReferenceMap(datasource);
      HeaderRepository headerRepository = new PostgresHeaderRepository(datasource);
      Repository<Long, Node> nodeRepository = new PostgresNodeRepository(datasource);
      Repository<Long, Way> wayRepository = new PostgresWayRepository(datasource);
      Repository<Long, Relation> relationRepository = new PostgresRelationRepository(datasource);
      var action =
          new UpdateService(
              coordinates,
              references,
              headerRepository,
              nodeRepository,
              wayRepository,
              relationRepository,
              databaseSrid);
      action.call();
      logger.info("Finished updating {}", database);
    } catch (Exception e) {
      logger.error("Failed updating {}", database);
      throw new WorkflowException(e);
    }
  }

  public URL resolve(String replicationUrl, Long sequenceNumber, String extension)
      throws MalformedURLException {
    String s = String.format("%09d", sequenceNumber);
    String uri =
        String.format(
            "%s/%s/%s/%s.%s",
            replicationUrl, s.substring(0, 3), s.substring(3, 6), s.substring(6, 9), extension);
    return URI.create(uri).toURL();
  }
}
