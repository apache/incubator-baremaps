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

package com.baremaps.cli;

import com.baremaps.importer.database.DataUpdater;
import com.baremaps.importer.database.DeltaProducer;
import com.baremaps.importer.database.HeaderTable;
import com.baremaps.importer.database.NodeTable;
import com.baremaps.importer.database.RelationTable;
import com.baremaps.importer.database.WayTable;
import com.baremaps.importer.geometry.ChangeGeometryBuilder;
import com.baremaps.importer.geometry.ProjectionTransformer;
import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.StateReader;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.State;
import com.baremaps.util.postgis.PostgisHelper;
import com.baremaps.util.storage.BlobStore;
import com.baremaps.util.tile.Tile;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
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
  private int zoom = 14;

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

    NodeTable nodeStore = new NodeTable(datasource);
    WayTable wayStore = new WayTable(datasource);
    RelationTable relationStore = new RelationTable(datasource);

    HeaderTable headerMapper = new HeaderTable(datasource);
    Header header = headerMapper.getLast();
    long nextSequenceNumber = header.getReplicationSequenceNumber() + 1;

    BlobStore blobStore = mixins.blobStore();

    logger.info("Downloading changes");
    String changePath = path(nextSequenceNumber) + ".osc.gz";
    URI changeURI = new URI(String.format("%s/%s", input, changePath));

    logger.info("Downloading state information");
    String statePath = path(nextSequenceNumber) + ".state.txt";
    URI stateURI = new URI(String.format("%s/%s", input, statePath));

    logger.info("Downloading diff file");
    Path path = blobStore.fetch(changeURI);

    logger.info("Updating database");
    ChangeGeometryBuilder changeGeometryBuilder = new ChangeGeometryBuilder(geometryFactory);
    ProjectionTransformer projectionTransformer = new ProjectionTransformer(coordinateTransform);
    DeltaProducer deltaProducer = new DeltaProducer(nodeStore, wayStore, relationStore, projectionTransformer, zoom);
    DataUpdater dataUpdater = new DataUpdater(nodeStore, wayStore, relationStore);
    OpenStreetMap.changeStream(path)
        .peek(changeGeometryBuilder)
        .peek(deltaProducer)
        .forEach(dataUpdater);

    logger.info("Saving differences");
    try (PrintWriter diffPrintWriter = new PrintWriter(blobStore.write(delta))) {
      for (Tile tile : deltaProducer.getTiles()) {
        diffPrintWriter.println(String.format("%d/%d/%d", tile.x(), tile.y(), tile.z()));
      }
    }

    logger.info("Updating state information");
    try (InputStream inputStream = blobStore.read(stateURI)) {
      State state = new StateReader(inputStream).read();
      headerMapper.insert(new Header(
          state.getTimestamp(),
          state.getSequenceNumber(),
          header.getReplicationUrl(),
          header.getSource(),
          header.getWritingProgram()));
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
