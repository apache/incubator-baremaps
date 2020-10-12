/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baremaps.osm.reader.pbf;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.CacheBuilder;
import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.reader.Reader;
import com.baremaps.osm.reader.ReaderException;
import java.nio.file.Path;
import java.util.List;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.proj4j.CoordinateTransform;

public class FileBlockGeometryReader implements Reader<FileBlockHandler> {

  private static Logger logger = LogManager.getLogger();

  private final GeometryFactory geometryFactory;

  private final CoordinateTransform coordinateTransform;

  private final Cache<Long, Coordinate> coordinateCache;

  private final Cache<Long, List<Long>> referencesCache;

  public FileBlockGeometryReader(
      GeometryFactory geometryFactory,
      CoordinateTransform coordinateTransform,
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referencesCache) {
    this.geometryFactory = geometryFactory;
    this.coordinateTransform = coordinateTransform;
    this.coordinateCache = coordinateCache;
    this.referencesCache = referencesCache;
  }

  public void read(Path path, FileBlockHandler handler) throws ReaderException {
    FileBlockReader reader = new FileBlockReader();

    // Parse the file a first time to create the cache
    logger.info("Initializing cache");
    NodeBuilder nodeBuilder = new NodeBuilder(geometryFactory, coordinateTransform);
    CacheBuilder cacheImportHandler = new CacheBuilder(nodeBuilder, coordinateCache, referencesCache);
    reader.read(path, cacheImportHandler);

    // Parse the file a second time to denormalize the geometries
    logger.info("Parsing geometries");
    WayBuilder wayBuilder = new WayBuilder(geometryFactory, coordinateCache);
    RelationBuilder relationBuilder = new RelationBuilder(geometryFactory, coordinateCache, referencesCache);
    FileBlockGeometryHandler decorator = new FileBlockGeometryHandler(nodeBuilder, wayBuilder, relationBuilder, handler);
    reader.read(path, decorator);
  }

}
