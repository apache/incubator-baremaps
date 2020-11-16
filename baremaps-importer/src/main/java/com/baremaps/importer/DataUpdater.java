package com.baremaps.importer;

import com.baremaps.importer.cache.Cache;
import com.baremaps.importer.database.DeltaProducer;
import com.baremaps.importer.database.HeaderTable;
import com.baremaps.importer.database.NodeTable;
import com.baremaps.importer.database.RelationTable;
import com.baremaps.importer.database.UpdateHandler;
import com.baremaps.importer.database.WayTable;
import com.baremaps.importer.geometry.GeometryBuilder;
import com.baremaps.importer.geometry.ProjectionTransformer;
import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.stream.StreamException;
import com.baremaps.util.tile.Tile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;

public class DataUpdater {

  private static Logger logger = LogManager.getLogger();

  private final ProjectionTransformer projectionTransformer;
  private final Cache<Long, Coordinate> coordinateCache;
  private final Cache<Long, List<Long>> referenceCache;
  private final HeaderTable headerTable;
  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;

  public DataUpdater(
      ProjectionTransformer projectionTransformer,
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referenceCache,
      HeaderTable headerTable,
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable) {
    this.projectionTransformer = projectionTransformer;
    this.coordinateCache = coordinateCache;
    this.referenceCache = referenceCache;
    this.headerTable = headerTable;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
  }

  public Set<Tile> execute(Path path) throws IOException {
    logger.info("Updating database");
    try {
      GeometryBuilder geometryBuilder = new GeometryBuilder(coordinateCache, referenceCache);
      DeltaProducer deltaProducer = new DeltaProducer(nodeTable, wayTable, relationTable, projectionTransformer, 1);
      UpdateHandler dataUpdater = new UpdateHandler(headerTable, nodeTable, wayTable, relationTable);
      OpenStreetMap.changeStream(path)
          .peek(change -> change.getElements().forEach(geometryBuilder))
          .peek(change -> change.getElements().forEach(projectionTransformer))
          .peek(deltaProducer)
          .forEach(dataUpdater);
      return deltaProducer.getTiles();
    } catch (StreamException e) {
      throw new IOException(e.getCause());
    }
  }

}
