package com.baremaps.importer;

import com.baremaps.importer.cache.Cache;
import com.baremaps.importer.database.HeaderTable;
import com.baremaps.importer.database.NodeTable;
import com.baremaps.importer.database.RelationTable;
import com.baremaps.importer.database.TileHandler;
import com.baremaps.importer.database.UpdateHandler;
import com.baremaps.importer.database.WayTable;
import com.baremaps.importer.geometry.GeometryHandler;
import com.baremaps.importer.geometry.ProjectionTransformer;
import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.State;
import com.baremaps.util.storage.BlobStore;
import com.baremaps.util.tile.Tile;
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
    TileHandler tileHandler = new TileHandler(nodeTable, wayTable, relationTable, new ProjectionTransformer(srid, 4326), zoom);
    UpdateHandler updateHandler = new UpdateHandler(headerTable, nodeTable, wayTable, relationTable);
    OpenStreetMap.streamXmlChanges(changeFilePath, false)
        .peek(change -> change.getElements().forEach(geometryHandler))
        .peek(change -> change.getElements().forEach(projectionTransformer))
        .peek(tileHandler)
        .forEach(updateHandler);
    State state = OpenStreetMap.readState(stateFile);
    headerTable.insert(new Header(
        state.getTimestamp(),
        state.getSequenceNumber(),
        header.getReplicationUrl(),
        header.getSource(),
        header.getWritingProgram()));

    return tileHandler.getTiles();
  }
}
