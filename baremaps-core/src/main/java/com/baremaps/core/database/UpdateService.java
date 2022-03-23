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

package com.baremaps.core.database;

import static com.baremaps.stream.ConsumerUtils.consumeThenReturn;

import com.baremaps.blob.Blob;
import com.baremaps.blob.BlobStore;
import com.baremaps.collection.LongDataMap;
import com.baremaps.core.database.repository.HeaderRepository;
import com.baremaps.core.database.repository.Repository;
import com.baremaps.osm.change.OsmChangeParser;
import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.State;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.function.ChangeEntityConsumer;
import com.baremaps.osm.function.CreateGeometryConsumer;
import com.baremaps.osm.function.ReprojectEntityConsumer;
import com.baremaps.osm.progress.InputStreamProgress;
import com.baremaps.osm.progress.ProgressLogger;
import com.baremaps.osm.state.OsmStateParser;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import org.locationtech.jts.geom.Coordinate;

public class UpdateService implements Callable<Void> {

  private final BlobStore blobStore;
  private final LongDataMap<Coordinate> coordinates;
  private final LongDataMap<List<Long>> references;
  private final HeaderRepository headerRepository;
  private final Repository<Long, Node> nodeRepository;
  private final Repository<Long, Way> wayRepository;
  private final Repository<Long, Relation> relationRepository;
  private final int srid;

  public UpdateService(
      BlobStore blobStore,
      LongDataMap<Coordinate> coordinates,
      LongDataMap<List<Long>> references,
      HeaderRepository headerRepository,
      Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository,
      int srid) {
    this.blobStore = blobStore;
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

    URI changeUri = resolve(replicationUrl, sequenceNumber, "osc.gz");
    Blob changeBlob = blobStore.get(changeUri);
    ProgressLogger progressLogger = new ProgressLogger(changeBlob.getContentLength(), 5000);
    try (InputStream blobInputStream = changeBlob.getInputStream();
        InputStream progressInputStream = new InputStreamProgress(blobInputStream, progressLogger);
        InputStream gzipInputStream = new GZIPInputStream(progressInputStream)) {
      new OsmChangeParser().changes(gzipInputStream).map(prepareChange).forEach(saveChange);
    }

    URI stateUri = resolve(replicationUrl, sequenceNumber, "state.txt");
    Blob stateBlob = blobStore.get(stateUri);
    try (InputStream stateInputStream = stateBlob.getInputStream()) {
      State state = new OsmStateParser().state(stateInputStream);
      headerRepository.put(
          new Header(
              state.getSequenceNumber(),
              state.getTimestamp(),
              header.getReplicationUrl(),
              header.getSource(),
              header.getWritingProgram()));
    }

    return null;
  }

  public URI resolve(String replicationUrl, Long sequenceNumber, String extension)
      throws URISyntaxException {
    String s = String.format("%09d", sequenceNumber);
    return new URI(
        String.format(
            "%s/%s/%s/%s.%s",
            replicationUrl, s.substring(0, 3), s.substring(3, 6), s.substring(6, 9), extension));
  }
}
