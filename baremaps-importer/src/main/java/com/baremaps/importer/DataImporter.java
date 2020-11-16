package com.baremaps.importer;

import com.baremaps.importer.cache.Cache;
import com.baremaps.importer.cache.CacheImporter;
import com.baremaps.importer.database.HeaderTable;
import com.baremaps.importer.database.ImportHandler;
import com.baremaps.importer.database.NodeTable;
import com.baremaps.importer.database.RelationTable;
import com.baremaps.importer.database.WayTable;
import com.baremaps.importer.geometry.GeometryBuilder;
import com.baremaps.importer.geometry.ProjectionTransformer;
import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.domain.Element;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;

public class DataImporter {

  private static Logger logger = LogManager.getLogger();

  private final ProjectionTransformer projectionTransformer;
  private final Cache<Long, Coordinate> coordinateCache;
  private final Cache<Long, List<Long>> referenceCache;
  private final HeaderTable headerTable;
  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;

  public DataImporter(
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

  public void execute(Path path) throws IOException {
    logger.info("Creating cache");
    try (CacheImporter cacheImporter = new CacheImporter(coordinateCache, referenceCache)) {
      OpenStreetMap.entityStream(path)
          .forEach(cacheImporter);
    }

    logger.info("Importing data");
    try (ImportHandler importHandler = new ImportHandler(headerTable, nodeTable, wayTable, relationTable)) {
      GeometryBuilder geometryBuilder = new GeometryBuilder(coordinateCache, referenceCache);
      OpenStreetMap.entityStream(path)
          .peek(geometryBuilder)
          .peek(projectionTransformer)
          .forEach(importHandler);
    }
  }


}
