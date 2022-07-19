package com.baremaps.workflow.tasks;

import static com.baremaps.stream.ConsumerUtils.consumeThenReturn;

import com.baremaps.collection.LongDataMap;
import com.baremaps.database.SaveChangeConsumer;
import com.baremaps.database.UpdateService;
import com.baremaps.database.collection.PostgresCoordinateMap;
import com.baremaps.database.collection.PostgresReferenceMap;
import com.baremaps.database.postgres.PostgresUtils;
import com.baremaps.database.repository.HeaderRepository;
import com.baremaps.database.repository.PostgresHeaderRepository;
import com.baremaps.database.repository.PostgresNodeRepository;
import com.baremaps.database.repository.PostgresRelationRepository;
import com.baremaps.database.repository.PostgresWayRepository;
import com.baremaps.database.repository.Repository;
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
import com.baremaps.osm.state.StateReader;
import com.baremaps.osm.xml.XmlChangeReader;
import com.baremaps.workflow.Task;
import com.baremaps.workflow.WorkflowException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Coordinate;

public record UpdateOpenStreetMap(
    String database,
    Integer databaseSrid)
    implements Task {

  @Override
  public void run() {
    try {
      DataSource datasource = PostgresUtils.dataSource(database);
      LongDataMap<Coordinate> coordinates = new PostgresCoordinateMap(datasource);
      LongDataMap<List<Long>> references = new PostgresReferenceMap(datasource);
      HeaderRepository headerRepository = new PostgresHeaderRepository(datasource);
      Repository<Long, Node> nodeRepository = new PostgresNodeRepository(datasource);
      Repository<Long, Way> wayRepository = new PostgresWayRepository(datasource);
      Repository<Long, Relation> relationRepository = new PostgresRelationRepository(datasource);

      new UpdateService(
          coordinates,
          references,
          headerRepository,
          nodeRepository,
          wayRepository,
          relationRepository,
          databaseSrid)
          .call();
      Header header = headerRepository.selectLatest();
      String replicationUrl = header.getReplicationUrl();
      Long sequenceNumber = header.getReplicationSequenceNumber() + 1;

      Consumer<Entity> createGeometry = new CreateGeometryConsumer(coordinates, references);
      Consumer<Entity> reprojectGeometry = new ReprojectEntityConsumer(4326, databaseSrid);
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
        headerRepository.put(
            new Header(
                state.getSequenceNumber(),
                state.getTimestamp(),
                header.getReplicationUrl(),
                header.getSource(),
                header.getWritingProgram()));
      }

    } catch (Exception e) {
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
