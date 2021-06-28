package com.baremaps.osm;

import com.baremaps.blob.BlobStore;
import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.CacheImporter;
import com.baremaps.osm.database.DatabaseImporter;
import com.baremaps.osm.database.HeaderTable;
import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.database.RelationTable;
import com.baremaps.osm.database.WayTable;
import com.baremaps.osm.domain.Block;
import com.baremaps.osm.geometry.GeometryHandler;
import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.osm.handler.BlockEntityHandler;
import com.baremaps.osm.progress.InputStreamProgress;
import com.baremaps.osm.progress.ProgressLogger;
import com.baremaps.stream.StreamUtils;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
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
    logger.info("Importing data");

    CacheImporter cacheImporter = new CacheImporter(coordinateCache, referenceCache);
    DatabaseImporter databaseDatabaseImporter = new DatabaseImporter(headerTable, nodeTable, wayTable, relationTable);
    GeometryHandler geometryFetcher = new GeometryHandler(coordinateCache, referenceCache);
    ProjectionTransformer projectionTransformer = new ProjectionTransformer(4326, srid);
    BlockEntityHandler geometryFactory = new BlockEntityHandler(geometryFetcher.andThen(projectionTransformer));
    Consumer<Block> blockHandler = cacheImporter.andThen(geometryFactory);

    ProgressLogger progressLogger = new ProgressLogger(blobStore.size(file), 5000);
    try (InputStream inputStream = new InputStreamProgress(blobStore.read(this.file), progressLogger)) {
      Stream<Block> stream = OpenStreetMap.streamPbfBlocks(inputStream).peek(blockHandler);
      StreamUtils.batch(stream).forEach(databaseDatabaseImporter);
    }
  }

}
