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

package org.apache.baremaps.database;

import static org.apache.baremaps.stream.ConsumerUtils.consumeThenReturn;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.database.repository.HeaderRepository;
import org.apache.baremaps.database.repository.Repository;
import org.apache.baremaps.openstreetmap.function.ChangeEntityConsumer;
import org.apache.baremaps.openstreetmap.function.CreateGeometryConsumer;
import org.apache.baremaps.openstreetmap.function.ReprojectEntityConsumer;
import org.apache.baremaps.openstreetmap.model.Change;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.openstreetmap.model.Header;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.State;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.openstreetmap.state.StateReader;
import org.apache.baremaps.openstreetmap.xml.XmlChangeReader;
import org.locationtech.jts.geom.Coordinate;

public class UpdateService implements Callable<Void> {

  private final LongDataMap<Coordinate> coordinates;
  private final LongDataMap<List<Long>> references;
  private final HeaderRepository headerRepository;
  private final Repository<Long, Node> nodeRepository;
  private final Repository<Long, Way> wayRepository;
  private final Repository<Long, Relation> relationRepository;
  private final int srid;

  public UpdateService(LongDataMap<Coordinate> coordinates, LongDataMap<List<Long>> references,
      HeaderRepository headerRepository, Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository, Repository<Long, Relation> relationRepository,
      int srid) {
    this.coordinates = coordinates;
    this.references = references;
    this.headerRepository = headerRepository;
    this.nodeRepository = nodeRepository;
    this.wayRepository = wayRepository;
    this.relationRepository = relationRepository;
    this.srid = srid;
  }

  @Override
  public Void call() throws Exception {
    Header header = headerRepository.selectLatest();
    String replicationUrl = header.getReplicationUrl();
    Long sequenceNumber = header.getReplicationSequenceNumber() + 1;

    Consumer<Entity> createGeometry = new CreateGeometryConsumer(coordinates, references);
    Consumer<Entity> reprojectGeometry = new ReprojectEntityConsumer(4326, srid);
    Consumer<Change> prepareGeometries =
        new ChangeEntityConsumer(createGeometry.andThen(reprojectGeometry));
    Function<Change, Change> prepareChange = consumeThenReturn(prepareGeometries);
    Consumer<Change> saveChange =
        new SaveChangeConsumer(nodeRepository, wayRepository, relationRepository);

    URL changeUrl = resolve(replicationUrl, sequenceNumber, "osc.gz");
    try (InputStream changeInputStream =
        new GZIPInputStream(new BufferedInputStream(changeUrl.openStream()))) {
      new XmlChangeReader().stream(changeInputStream).map(prepareChange).forEach(saveChange);
    }

    URL stateUrl = resolve(replicationUrl, sequenceNumber, "state.txt");
    try (InputStream stateInputStream = new BufferedInputStream(stateUrl.openStream())) {
      State state = new StateReader().state(stateInputStream);
      headerRepository.put(new Header(state.getSequenceNumber(), state.getTimestamp(),
          header.getReplicationUrl(), header.getSource(), header.getWritingProgram()));
    }

    return null;
  }

  public URL resolve(String replicationUrl, Long sequenceNumber, String extension)
      throws MalformedURLException {
    String s = String.format("%09d", sequenceNumber);
    String uri = String.format("%s/%s/%s/%s.%s", replicationUrl, s.substring(0, 3),
        s.substring(3, 6), s.substring(6, 9), extension);
    return URI.create(uri).toURL();
  }
}
