package com.baremaps.osm;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.database.HeaderTable;
import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.database.RelationTable;
import com.baremaps.osm.database.ChangeTiler;
import com.baremaps.osm.database.DatabaseUpdater;
import com.baremaps.osm.database.WayTable;
import com.baremaps.osm.geometry.GeometryHandler;
import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.State;
import com.baremaps.core.storage.BlobStore;
import com.baremaps.core.tile.Tile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateTask {

  private static Logger logger = LoggerFactory.getLogger(UpdateTask.class);

  private final BlobStore blobStore;
  private final Cache<Long, Coordinate> coordinateCache;
  private final Cache<Long, List<Long>> referenceCache;
  private final HeaderTable headerTable;
  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;
  private final int srid;
  private final int zoom;

  public UpdateTask(
      BlobStore blobStore,
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referenceCache,
      HeaderTable headerTable,
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable,
      int srid,
      int zoom) {
    this.blobStore = blobStore;
    this.coordinateCache = coordinateCache;
    this.referenceCache = referenceCache;
    this.headerTable = headerTable;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
    this.srid = srid;
    this.zoom = zoom;
  }


  public URI resolve(String replicationUrl, Long sequenceNumber, String extension) throws URISyntaxException {
    String s = String.format("%09d", sequenceNumber);
    return new URI(String.format("%s/%s/%s/%s.%s",
        replicationUrl,
        s.substring(0, 3),
        s.substring(3, 6),
        s.substring(6, 9),
        extension));
  }

  public Set<Tile> execute() throws Exception {
    Header header = headerTable.latest();
    String replicationUrl = header.getReplicationUrl();
    Long sequenceNumber = header.getReplicationSequenceNumber() + 1;

    URI changeFileUri = resolve(replicationUrl, sequenceNumber, "osc.gz");
    Path changeFilePath = blobStore.fetch(changeFileUri);

    URI stateFileUri = resolve(replicationUrl, sequenceNumber, "state.txt");
    Path stateFile = blobStore.fetch(stateFileUri);

    logger.info("Importing changes and state in database");
    GeometryHandler geometryHandler = new GeometryHandler(coordinateCache, referenceCache);
    ProjectionTransformer projectionTransformer = new ProjectionTransformer(4326, srid);
    ChangeTiler changeTiler = new ChangeTiler(nodeTable, wayTable, relationTable, new ProjectionTransformer(srid, 4326), zoom);
    DatabaseUpdater databaseUpdater = new DatabaseUpdater(headerTable, nodeTable, wayTable, relationTable);
    OpenStreetMap.streamXmlChanges(changeFilePath, false)
        .peek(change -> change.getElements().forEach(geometryHandler))
        .peek(change -> change.getElements().forEach(projectionTransformer))
        .peek(changeTiler)
        .forEach(databaseUpdater);
    State state = OpenStreetMap.readState(stateFile);
    headerTable.insert(new Header(
        state.getTimestamp(),
        state.getSequenceNumber(),
        header.getReplicationUrl(),
        header.getSource(),
        header.getWritingProgram()));

    return changeTiler.getTiles();
  }
}
