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

package com.baremaps.cli.command;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.PostgisCoordinateCache;
import com.baremaps.osm.cache.PostgisReferenceCache;
import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.State;
import com.baremaps.osm.parser.XMLChangeParser;
import com.baremaps.osm.store.PostgisHeaderStore;
import com.baremaps.osm.store.PostgisNodeStore;
import com.baremaps.osm.store.PostgisRelationStore;
import com.baremaps.osm.store.PostgisWayStore;
import com.baremaps.osm.store.StoreDeltaHandler;
import com.baremaps.osm.store.StoreUpdateHandler;
import com.baremaps.tiles.Tile;
import com.baremaps.util.postgis.PostgisHelper;
import com.baremaps.util.storage.BlobStore;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "update", description = "Update OpenStreetMap data in the Postgresql database.")
public class Update implements Callable<Integer> {

  private static Logger logger = LogManager.getLogger();

  @Mixin
  private Mixins mixins;

  @Option(
      names = {"--input"},
      paramLabel = "OSC",
      description = "The OpenStreetMap Change file.",
      required = true)
  private String input;

  @Option(
      names = {"--database"},
      paramLabel = "JDBC",
      description = "The JDBC url of the Postgres database.",
      required = true)
  private String database;

  @Option(
      names = {"--delta"},
      paramLabel = "DELTA",
      description = "The output delta file.",
      required = true)
  private URI delta;

  @Option(
      names = {"--zoom"},
      paramLabel = "ZOOM",
      description = "The zoom level.")
  private int zoom = 16;

  @Override
  public Integer call() throws Exception {
    Configurator.setRootLevel(Level.getLevel(mixins.logLevel.name()));
    logger.info("{} processors available.", Runtime.getRuntime().availableProcessors());

    PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);

    CRSFactory crsFactory = new CRSFactory();
    CoordinateReferenceSystem sourceCRS = crsFactory.createFromName("EPSG:4326");
    CoordinateReferenceSystem targetCRS = crsFactory.createFromName("EPSG:3857");
    CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
    CoordinateTransform coordinateTransform = coordinateTransformFactory
        .createTransform(sourceCRS, targetCRS);
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 3857);

    Cache<Long, Coordinate> coordinateCache = new PostgisCoordinateCache(datasource);
    Cache<Long, List<Long>> referenceCache = new PostgisReferenceCache(datasource);

    NodeBuilder nodeBuilder = new NodeBuilder(geometryFactory, coordinateTransform);
    WayBuilder wayBuilder = new WayBuilder(geometryFactory, coordinateCache);
    RelationBuilder relationBuilder = new RelationBuilder(geometryFactory, coordinateCache, referenceCache);

    PostgisNodeStore nodeStore = new PostgisNodeStore(datasource);
    PostgisWayStore wayStore = new PostgisWayStore(datasource);
    PostgisRelationStore relationStore = new PostgisRelationStore(datasource);

    PostgisHeaderStore headerMapper = new PostgisHeaderStore(datasource);
    Header header = headerMapper.getLast();
    long nextSequenceNumber = header.getReplicationSequenceNumber() + 1;

    BlobStore blobStore = mixins.blobStore();

    logger.info("Downloading changes");
    String changePath = path(nextSequenceNumber) + ".osc.gz";
    URI changeURI = new URI(String.format("%s/%s", input, changePath));

    logger.info("Downloading state information");
    String statePath = path(nextSequenceNumber) + ".state.txt";
    URI stateURI = new URI(String.format("%s/%s", input, statePath));

    ProjectionTransformer projectionTransformer = new ProjectionTransformer(coordinateTransform);
    StoreDeltaHandler deltaHandler = new StoreDeltaHandler(
        nodeStore, wayStore, relationStore,
        projectionTransformer, zoom);

    Path path = blobStore.fetch(changeURI);

    logger.info("Computing differences");
    new XMLChangeParser().parse(path, deltaHandler);

    logger.info("Saving differences");
    try (PrintWriter diffPrintWriter = new PrintWriter(blobStore.write(delta))) {
      for (Tile tile : deltaHandler.getTiles()) {
        diffPrintWriter.println(String.format("%d/%d/%d", tile.x(), tile.y(), tile.z()));
      }
    }

    logger.info("Updating database");
    StoreUpdateHandler updateHandler = new StoreUpdateHandler(
        nodeBuilder, wayBuilder, relationBuilder,
        nodeStore, wayStore, relationStore);
    new XMLChangeParser().parse(path, updateHandler);

    logger.info("Updating state information");
    try (InputStreamReader reader = new InputStreamReader(blobStore.read(stateURI), Charsets.UTF_8)) {
      String stateContent = CharStreams.toString(reader);
      State state = State.parse(stateContent);
      headerMapper.insert(new Header(
          state.timestamp,
          state.sequenceNumber,
          header.getReplicationUrl(),
          header.getSource(),
          header.getWritingProgram(),
          header.getBbox()));
    }

    return 0;
  }

  public String path(long sequenceNumber) {
    String leading = String.format("%09d", sequenceNumber);
    return leading.substring(0, 3) + "/"
        + leading.substring(3, 6) + "/"
        + leading.substring(6, 9);
  }

}
