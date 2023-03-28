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

import static org.apache.baremaps.stream.ConsumerUtils.consumeThenReturn;

import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.baremaps.collection.DataMap;
import org.apache.baremaps.database.ChangeImporter;
import org.apache.baremaps.database.collection.PostgresCoordinateMap;
import org.apache.baremaps.database.collection.PostgresReferenceMap;
import org.apache.baremaps.database.repository.*;
import org.apache.baremaps.openstreetmap.function.ChangeEntitiesHandler;
import org.apache.baremaps.openstreetmap.function.EntityGeometryBuilder;
import org.apache.baremaps.openstreetmap.function.EntityProjectionTransformer;
import org.apache.baremaps.openstreetmap.model.Header;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.openstreetmap.state.StateReader;
import org.apache.baremaps.openstreetmap.xml.XmlChangeReader;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record UpdateOpenStreetMap(String database, Integer databaseSrid) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(UpdateOpenStreetMap.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var datasource = context.getDataSource(database);
    DataMap<Coordinate> coordinateMap = new PostgresCoordinateMap(datasource);
    DataMap<List<Long>> referenceMap = new PostgresReferenceMap(datasource);
    HeaderRepository headerRepository = new PostgresHeaderRepository(datasource);
    Repository<Long, Node> nodeRepository = new PostgresNodeRepository(datasource);
    Repository<Long, Way> wayRepository = new PostgresWayRepository(datasource);
    Repository<Long, Relation> relationRepository = new PostgresRelationRepository(datasource);
    execute(
        coordinateMap,
        referenceMap,
        headerRepository,
        nodeRepository,
        wayRepository,
        relationRepository,
        databaseSrid);
  }

  public static void execute(DataMap<Coordinate> coordinateMap, DataMap<List<Long>> referenceMap,
      HeaderRepository headerRepository, Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository, Repository<Long, Relation> relationRepository,
      int srid) throws Exception {
    var header = headerRepository.selectLatest();
    var replicationUrl = header.getReplicationUrl();
    var sequenceNumber = header.getReplicationSequenceNumber() + 1;

    var createGeometry = new EntityGeometryBuilder(coordinateMap, referenceMap);
    var reprojectGeometry = new EntityProjectionTransformer(4326, srid);
    var prepareGeometries = new ChangeEntitiesHandler(createGeometry.andThen(reprojectGeometry));
    var prepareChange = consumeThenReturn(prepareGeometries);
    var saveChange = new ChangeImporter(nodeRepository, wayRepository, relationRepository);

    var changeUrl = resolve(replicationUrl, sequenceNumber, "osc.gz");
    try (var changeInputStream =
        new GZIPInputStream(new BufferedInputStream(changeUrl.openStream()))) {
      new XmlChangeReader().stream(changeInputStream).map(prepareChange).forEach(saveChange);
    }

    var stateUrl = resolve(replicationUrl, sequenceNumber, "state.txt");
    try (var stateInputStream = new BufferedInputStream(stateUrl.openStream())) {
      var state = new StateReader().state(stateInputStream);
      headerRepository.put(new Header(state.getSequenceNumber(), state.getTimestamp(),
          header.getReplicationUrl(), header.getSource(), header.getWritingProgram()));
    }
  }

  public static URL resolve(String replicationUrl, Long sequenceNumber, String extension)
      throws MalformedURLException {
    var s = String.format("%09d", sequenceNumber);
    var uri = String.format("%s/%s/%s/%s.%s", replicationUrl, s.substring(0, 3), s.substring(3, 6),
        s.substring(6, 9), extension);
    return URI.create(uri).toURL();
  }
}
