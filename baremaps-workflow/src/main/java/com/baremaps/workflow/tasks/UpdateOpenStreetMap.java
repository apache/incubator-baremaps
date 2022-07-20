package com.baremaps.workflow.tasks;

import com.baremaps.collection.LongDataMap;
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
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.workflow.Task;
import com.baremaps.workflow.WorkflowException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
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
      var action = new UpdateService(
          coordinates,
          references,
          headerRepository,
          nodeRepository,
          wayRepository,
          relationRepository,
          databaseSrid);
      action.call();
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
