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
package com.baremaps.osm.parser;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.CacheImportFileBlockHandler;
import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import java.nio.file.Path;
import java.util.List;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.proj4j.CoordinateTransform;

public class PBFFileBlockGeometryParser {

  private static Logger logger = LogManager.getLogger();

  private final GeometryFactory geometryFactory;

  private final CoordinateTransform coordinateTransform;

  private final Cache<Long, Coordinate> coordinateCache;

  private final Cache<Long, List<Long>> referencesCache;

  @Inject
  public PBFFileBlockGeometryParser(
      GeometryFactory geometryFactory,
      CoordinateTransform coordinateTransform,
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referencesCache) {
    this.geometryFactory = geometryFactory;
    this.coordinateTransform = coordinateTransform;
    this.coordinateCache = coordinateCache;
    this.referencesCache = referencesCache;
  }

  public void parse(Path path, PBFFileBlockHandler handler) throws Exception {
    PBFFileBlockParser parser = new PBFFileBlockParser();

    // Parse the file a first time to create the cache
    NodeBuilder nodeBuilder = new NodeBuilder(geometryFactory, coordinateTransform);
    CacheImportFileBlockHandler cacheImportHandler = new CacheImportFileBlockHandler(nodeBuilder, coordinateCache, referencesCache);
    parser.parse(path, cacheImportHandler);

    // Parse the file a second time to denormalize the geometries
    WayBuilder wayBuilder = new WayBuilder(geometryFactory, coordinateCache);
    RelationBuilder relationBuilder = new RelationBuilder(geometryFactory, coordinateCache, referencesCache);
    PBFFileBlockGeometryHandler decorator = new PBFFileBlockGeometryHandler(nodeBuilder, wayBuilder, relationBuilder, handler);
    parser.parse(path, decorator);
  }

}
