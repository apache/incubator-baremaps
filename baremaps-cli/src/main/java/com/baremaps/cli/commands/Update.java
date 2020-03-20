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

import com.baremaps.core.io.InputStreams;
import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.osmpbf.HeaderBlock;
import com.baremaps.osm.osmxml.Change;
import com.baremaps.osm.osmxml.ChangeConsumer;
import com.baremaps.osm.osmxml.ChangeSpliterator;
import com.baremaps.osm.osmxml.State;
import com.baremaps.osm.postgis.PostgisHeaderStore;
import com.baremaps.osm.store.Store;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.baremaps.core.postgis.PostgisHelper;
import com.baremaps.osm.cache.PostgisCoordinateCache;
import com.baremaps.osm.cache.PostgisReferenceCache;
import com.baremaps.osm.postgis.PostgisNodeStore;
import com.baremaps.osm.postgis.PostgisRelationStore;
import com.baremaps.osm.postgis.PostgisWayStore;
import java.io.InputStream;
import java.io.InputStreamReader;
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
      paramLabel= "OSC",
      description = "The OpenStreetMap Change file.",
      required = true)
  private String input;

  @Option(
      names = {"--database"},
      paramLabel= "JDBC",
      description = "The JDBC url of the Postgres database.",
      required = true)
  private String database;

  @Override
  public Integer call() throws Exception {
    Configurator.setRootLevel(Level.getLevel(mixins.level));
    logger.info("{} processors available.", Runtime.getRuntime().availableProcessors());

    PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);

    CRSFactory crsFactory = new CRSFactory();
    CoordinateReferenceSystem epsg4326 = crsFactory.createFromName("EPSG:4326");
    CoordinateReferenceSystem epsg3857 = crsFactory.createFromName("EPSG:3857");
    CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
    CoordinateTransform coordinateTransform = coordinateTransformFactory.createTransform(epsg4326, epsg3857);
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 3857);

    Store<Long, Coordinate> coordinateStore = new PostgisCoordinateCache(datasource);
    Store<Long, List<Long>> referenceStore = new PostgisReferenceCache(datasource);
    
    PostgisHeaderStore headerMapper = new PostgisHeaderStore(datasource);
    PostgisNodeStore nodeStore = new PostgisNodeStore(datasource,
        new NodeBuilder(coordinateTransform, geometryFactory));
    PostgisWayStore wayStore = new PostgisWayStore(datasource,
        new WayBuilder(coordinateTransform, geometryFactory, coordinateStore));
    PostgisRelationStore relationStore = new PostgisRelationStore(datasource,
        new RelationBuilder(coordinateTransform, geometryFactory, coordinateStore, referenceStore));

    HeaderBlock header = headerMapper.getLast();
    long nextSequenceNumber = header.getReplicationSequenceNumber() + 1;
    String statePath = statePath(nextSequenceNumber);

    String stateURL = String.format("%s/%s", input, statePath);

    try (InputStreamReader reader = new InputStreamReader(InputStreams.from(stateURL), Charsets.UTF_8)) {
      String stateContent = CharStreams.toString(reader);
      State state = State.parse(stateContent);
      String changePath = changePath(nextSequenceNumber);
      String changeURL = String.format("%s/%s", input, changePath);

      try (InputStream changeInputStream = new GZIPInputStream(InputStreams.from(changeURL))) {
        Spliterator<Change> spliterator = new ChangeSpliterator(changeInputStream);
        Stream<Change> changeStream = StreamSupport.stream(spliterator, true);
        ChangeConsumer changeConsumer = new ChangeConsumer(nodeStore, wayStore, relationStore);
        changeStream.forEach(changeConsumer);
      }

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

  public String changePath(long sequenceNumber) {
    return path(sequenceNumber) + ".osc.gz";
  }

  public String statePath(long sequenceNumber) {
    return path(sequenceNumber) + ".state.txt";
  }

}
