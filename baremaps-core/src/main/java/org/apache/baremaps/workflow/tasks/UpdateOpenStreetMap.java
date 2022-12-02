/*
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

package org.apache.baremaps.workflow.tasks;

import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.database.UpdateService;
import org.apache.baremaps.database.collection.PostgresCoordinateMap;
import org.apache.baremaps.database.collection.PostgresReferenceMap;
import org.apache.baremaps.database.repository.*;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

public record UpdateOpenStreetMap(String database, Integer databaseSrid) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(UpdateOpenStreetMap.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    logger.info("Updating {}", database);
    var datasource = context.getDataSource(database);
    LongDataMap<Coordinate> coordinateMap = new PostgresCoordinateMap(datasource);
    LongDataMap<List<Long>> referenceMap = new PostgresReferenceMap(datasource);
    HeaderRepository headerRepository = new PostgresHeaderRepository(datasource);
    Repository<Long, Node> nodeRepository = new PostgresNodeRepository(datasource);
    Repository<Long, Way> wayRepository = new PostgresWayRepository(datasource);
    Repository<Long, Relation> relationRepository = new PostgresRelationRepository(datasource);
    var action =
      new UpdateService(
        coordinateMap,
        referenceMap,
        headerRepository,
        nodeRepository,
        wayRepository,
        relationRepository,
        databaseSrid
      );
    action.call();
    logger.info("Finished updating {}", database);
  }

  public URL resolve(String replicationUrl, Long sequenceNumber, String extension)
    throws MalformedURLException {
    String s = String.format("%09d", sequenceNumber);
    String uri =
      String.format(
        "%s/%s/%s/%s.%s",
        replicationUrl, s.substring(0, 3), s.substring(3, 6), s.substring(6, 9), extension
      );
    return URI.create(uri).toURL();
  }
}
