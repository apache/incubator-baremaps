package com.baremaps.osm;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.DownloadManager;
import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.CacheImporter;
import com.baremaps.osm.database.DatabaseImporter;
import com.baremaps.osm.database.HeaderTable;
import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.database.RelationTable;
import com.baremaps.osm.database.WayTable;
import com.baremaps.osm.geometry.GeometryHandler;
import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.osm.handler.BlockEntityHandler;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportTask {

  private static Logger logger = LoggerFactory.getLogger(ImportTask.class);

  private final URI file;
  private final BlobStore blobStore;
  private final Cache<Long, Coordinate> coordinateCache;
  private final Cache<Long, List<Long>> referenceCache;
  private final HeaderTable headerTable;
  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;
  private final int srid;

  public ImportTask(
      URI file,
      BlobStore blobStore,
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referenceCache,
      HeaderTable headerTable,
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable,
      int srid) {
    this.file = file;
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
    logger.info("Fetch the file");
    Path path = new DownloadManager(blobStore).download(this.file);

    logger.info("Creating cache");
    CacheImporter cacheImporter = new CacheImporter(coordinateCache, referenceCache);



    try (InputStream cacheInputStream = new BufferedInputStream(Files.newInputStream(path))) {
      OpenStreetMap.streamPbfBlocks(cacheInputStream, false).forEach(cacheImporter);
    }

    logger.info("Importing data");
    DatabaseImporter databaseDatabaseImporter = new DatabaseImporter(headerTable, nodeTable, wayTable, relationTable);
    GeometryHandler geometryHandler = new GeometryHandler(coordinateCache, referenceCache);
    ProjectionTransformer projectionTransformer = new ProjectionTransformer(4326, srid);
    try (InputStream dataInputStream = new BufferedInputStream(Files.newInputStream(path))) {
      OpenStreetMap.streamPbfBlocks(dataInputStream, true)
          .peek(new BlockEntityHandler(geometryHandler.andThen(projectionTransformer)))
          .forEach(databaseDatabaseImporter);
    }
  }

}
