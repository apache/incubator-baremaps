/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.cli.commands;

import com.baremaps.core.fetch.Data;
import com.baremaps.core.fetch.Fetcher;
import com.baremaps.core.postgis.PostgisHelper;
import com.baremaps.core.tile.Tile;
import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.PostgisCoordinateCache;
import com.baremaps.osm.cache.PostgisReferenceCache;
import com.baremaps.osm.database.HeaderTable;
import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.database.RelationTable;
import com.baremaps.osm.database.WayTable;
import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.osmpbf.HeaderBlock;
import com.baremaps.osm.osmxml.Change;
import com.baremaps.osm.osmxml.ChangeSpliterator;
import com.baremaps.osm.stream.DeltaProducer;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
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

@Command(name = "delta", description = "List the tiles affected by an OpenStreetMap change file.")
public class Delta implements Callable<Integer> {

  private static Logger logger = LogManager.getLogger();

  @Mixin
  private Mixins mixins;

  @Option(
      names = {"--input"},
      paramLabel = "OSC",
      description = "The input change file.",
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
      description = "The input delta file.",
      required = false)
  private String delta;

  @Override
  public Integer call() throws Exception {
    Configurator.setRootLevel(Level.getLevel(mixins.level));
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
    RelationBuilder relationBuilder = new RelationBuilder(coordinateCache, referenceCache);

    NodeTable nodeStore = new NodeTable(datasource);
    WayTable wayStore = new WayTable(datasource);
    RelationTable relationStore = new RelationTable(datasource);

    HeaderTable headerTable = new HeaderTable(datasource);
    HeaderBlock headerBlock = headerTable.getLast();
    long nextSequenceNumber = headerBlock.getReplicationSequenceNumber() + 1;

    logger.info("Downloading changes.");
    String changePath = changePath(nextSequenceNumber);
    String changeURL = String.format("%s/%s", input, changePath);
    Fetcher fetcher = new Fetcher(mixins.caching);
    Data changeFetch = fetcher.fetch(changeURL);

    ProjectionTransformer projectionTransformer = new ProjectionTransformer(coordinateTransformFactory
        .createTransform(targetCRS, sourceCRS));
    DeltaProducer deltaMaker = new DeltaProducer(projectionTransformer, nodeBuilder, wayBuilder, relationBuilder, nodeStore, wayStore, relationStore);

    logger.info("Computing differences.");
    try (InputStream changeInputStream = new GZIPInputStream(changeFetch.getInputStream())) {
      Spliterator<Change> spliterator = new ChangeSpliterator(changeInputStream);
      Stream<Change> changeStream = StreamSupport.stream(spliterator, true);
      changeStream.forEach(deltaMaker);
    }

    logger.info("Saving differences.");
    try (PrintWriter diffPrintWriter = new PrintWriter(new FileOutputStream(Paths.get(delta).toFile()))) {
      for (Tile tile : deltaMaker.getTiles()) {
        diffPrintWriter.println(String.format("%d/%d/%d", tile.getX(), tile.getY(), tile.getZ()));
      }
    }

    return 0;
  }

  public String path(long sequenceNumber) {
    String leading = String.format("%09d", sequenceNumber);
    return leading.substring(0, 3) + "/"
        + leading.substring(3, 6) + "/"
        + leading.substring(6, 9);
  }

  public String changePath(long sequenceNumber) {
    return path(sequenceNumber) + ".osc.gz";
  }

}