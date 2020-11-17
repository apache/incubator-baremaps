package com.baremaps.importer;

import com.baremaps.importer.cache.Cache;
import com.baremaps.importer.database.HeaderTable;
import com.baremaps.importer.database.NodeTable;
import com.baremaps.importer.database.RelationTable;
import com.baremaps.importer.database.TileHandler;
import com.baremaps.importer.database.UpdateHandler;
import com.baremaps.importer.database.WayTable;
import com.baremaps.importer.geometry.GeometryBuilder;
import com.baremaps.importer.geometry.ProjectionHandler;
import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.State;
import com.baremaps.util.storage.BlobStore;
import com.baremaps.util.tile.Tile;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;

public class UpdateTask {

  private static Logger logger = LogManager.getLogger();

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

    logger.info("Downloading data");
    Path changeFile = blobStore.fetch(resolve(replicationUrl, sequenceNumber, "osc.gz"));
    Path stateFile = blobStore.fetch(resolve(replicationUrl, sequenceNumber, "state.txt"));

    logger.info("Importing changes");
    GeometryBuilder geometryBuilder = new GeometryBuilder(coordinateCache, referenceCache);
    ProjectionHandler projectionHandler = new ProjectionHandler(srid);
    TileHandler tileHandler = new TileHandler(nodeTable, wayTable, relationTable, projectionHandler, zoom);
    UpdateHandler updateHandler = new UpdateHandler(headerTable, nodeTable, wayTable, relationTable);
    OpenStreetMap.changeStream(changeFile)
        .peek(change -> change.getElements().forEach(geometryBuilder))
        .peek(change -> change.getElements().forEach(projectionHandler))
        .peek(tileHandler)
        .forEach(updateHandler);

    logger.info("Updating state");
    State state = OpenStreetMap.state(stateFile);
    headerTable.insert(new Header(
        state.getTimestamp(),
        state.getSequenceNumber(),
        header.getReplicationUrl(),
        header.getSource(),
        header.getWritingProgram()));

    return tileHandler.getTiles();
  }
}
