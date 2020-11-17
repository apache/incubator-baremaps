package com.baremaps.importer;

import com.baremaps.importer.cache.Cache;
import com.baremaps.importer.cache.CacheHandler;
import com.baremaps.importer.database.DatabaseHandler;
import com.baremaps.importer.database.HeaderTable;
import com.baremaps.importer.database.NodeTable;
import com.baremaps.importer.database.RelationTable;
import com.baremaps.importer.database.WayTable;
import com.baremaps.importer.geometry.GeometryBuilder;
import com.baremaps.importer.geometry.ProjectionHandler;
import com.baremaps.osm.OpenStreetMap;
import com.baremaps.util.storage.BlobStore;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;

public class ImportTask {

  private static Logger logger = LogManager.getLogger();

  private final URI uri;
  private final BlobStore blobStore;
  private final Cache<Long, Coordinate> coordinateCache;
  private final Cache<Long, List<Long>> referenceCache;
  private final HeaderTable headerTable;
  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;
  private final int srid;

  public ImportTask(
      URI uri,
      BlobStore blobStore,
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referenceCache,
      HeaderTable headerTable,
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable,
      int srid) {
    this.uri = uri;
    this.blobStore = blobStore;
    this.coordinateCache = coordinateCache;
    this.referenceCache = referenceCache;
    this.headerTable = headerTable;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
    this.srid = srid;
  }

  public void execute() throws Exception {
    logger.info("Downloading data");
    Path path = blobStore.fetch(uri);

    logger.info("Creating cache");
    try (CacheHandler cacheHandler = new CacheHandler(coordinateCache, referenceCache)) {
      OpenStreetMap.entityStream(path).forEach(cacheHandler);
    }

    logger.info("Importing data");
    try (DatabaseHandler databaseHandler = new DatabaseHandler(headerTable, nodeTable, wayTable, relationTable)) {
      GeometryBuilder geometryBuilder = new GeometryBuilder(coordinateCache, referenceCache);
      ProjectionHandler projectionHandler = new ProjectionHandler(srid);
      OpenStreetMap.entityStream(path)
          .peek(geometryBuilder)
          .peek(projectionHandler)
          .forEach(databaseHandler);
    }
  }
}
