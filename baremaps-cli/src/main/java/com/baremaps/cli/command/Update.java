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
import com.baremaps.osm.store.StoreDiffer;
import com.baremaps.osm.store.StoreUpdater;
import com.baremaps.osm.store.PostgisHeaderStore;
import com.baremaps.osm.store.PostgisNodeStore;
import com.baremaps.osm.store.PostgisRelationStore;
import com.baremaps.osm.store.PostgisWayStore;
import com.baremaps.osm.geometry.NodeGeometryBuilder;
import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.osm.geometry.RelationGeometryBuilder;
import com.baremaps.osm.geometry.WayGeometryBuilder;
import com.baremaps.osm.pbf.HeaderBlock;
import com.baremaps.osm.model.Change;
import com.baremaps.osm.xml.ChangeSpliterator;
import com.baremaps.osm.xml.State;
import com.baremaps.tiles.Tile;
import com.baremaps.util.postgis.PostgisHelper;
import com.baremaps.util.vfs.FileSystem;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;
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

    NodeGeometryBuilder nodeGeometryBuilder = new NodeGeometryBuilder(geometryFactory, coordinateTransform);
    WayGeometryBuilder wayGeometryBuilder = new WayGeometryBuilder(geometryFactory, coordinateCache);
    RelationGeometryBuilder relationGeometryBuilder = new RelationGeometryBuilder(geometryFactory, coordinateCache, referenceCache);

    PostgisNodeStore nodeStore = new PostgisNodeStore(datasource);
    PostgisWayStore wayStore = new PostgisWayStore(datasource);
    PostgisRelationStore relationStore = new PostgisRelationStore(datasource);

    PostgisHeaderStore headerMapper = new PostgisHeaderStore(datasource);
    HeaderBlock header = headerMapper.getLast();
    long nextSequenceNumber = header.getReplicationSequenceNumber() + 1;

    FileSystem fileSystem = mixins.filesystem();

    logger.info("Downloading changes");
    String changePath =  path(nextSequenceNumber) + ".osc.gz";
    URI changeURI = new URI(String.format("%s/%s", input, changePath));

    logger.info("Downloading state information");
    String statePath = path(nextSequenceNumber) + ".state.txt";;
    URI stateURI = new URI(String.format("%s/%s", input, statePath));

    ProjectionTransformer projectionTransformer = new ProjectionTransformer(coordinateTransformFactory
        .createTransform(targetCRS, sourceCRS));
    StoreDiffer deltaMaker = new StoreDiffer(nodeGeometryBuilder, wayGeometryBuilder, relationGeometryBuilder, nodeStore,
        wayStore, relationStore, projectionTransformer, zoom);

    logger.info("Computing differences");
    try (InputStream changeInputStream = new GZIPInputStream(fileSystem.read(changeURI))) {
      Spliterator<Change> spliterator = new ChangeSpliterator(changeInputStream);
      Stream<Change> changeStream = StreamSupport.stream(spliterator, true);
      changeStream.forEach(deltaMaker);
    }

    logger.info("Saving differences");
    try (PrintWriter diffPrintWriter = new PrintWriter(fileSystem.write(delta))) {
      for (Tile tile : deltaMaker.getTiles()) {
        diffPrintWriter.println(String.format("%d/%d/%d", tile.x(), tile.y(), tile.z()));
      }
    }

    logger.info("Updating database");
    try (InputStream changeInputStream = new GZIPInputStream(fileSystem.read(changeURI))) {
      Spliterator<Change> spliterator = new ChangeSpliterator(changeInputStream);
      Stream<Change> changeStream = StreamSupport.stream(spliterator, true);
      StoreUpdater databaseUpdater = new StoreUpdater(nodeGeometryBuilder, wayGeometryBuilder,
          relationGeometryBuilder,
          nodeStore, wayStore, relationStore);
      changeStream.forEach(databaseUpdater);
    }

    logger.info("Updating state information");
    try (InputStreamReader reader = new InputStreamReader(fileSystem.read(stateURI), Charsets.UTF_8)) {
      String stateContent = CharStreams.toString(reader);
      State state = State.parse(stateContent);
      headerMapper.insert(new HeaderBlock(
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
