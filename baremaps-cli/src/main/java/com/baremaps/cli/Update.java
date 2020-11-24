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

import com.baremaps.importer.UpdateTask;
import com.baremaps.importer.cache.PostgresCoordinateCache;
import com.baremaps.importer.cache.PostgresReferenceCache;
import com.baremaps.importer.database.HeaderTable;
import com.baremaps.importer.database.NodeTable;
import com.baremaps.importer.database.RelationTable;
import com.baremaps.importer.database.WayTable;
import com.baremaps.util.postgres.PostgresHelper;
import com.baremaps.util.storage.BlobStore;
import com.baremaps.util.tile.Tile;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "update", description = "Update OpenStreetMap data in the database.")
public class Update implements Callable<Integer> {

  private static Logger logger = LoggerFactory.getLogger(Update.class);

  @Mixin
  private Options options;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the database.",
      required = true)
  private String database;

  @Option(
      names = {"--tiles"},
      paramLabel = "TILES",
      description = "The tiles affected by the update.")
  private URI tiles;

  @Option(
      names = {"--zoom"},
      paramLabel = "ZOOM",
      description = "The zoom level used to compute the tiles.")
  private int zoom = 14;

  @Option(
      names = {"--srid"},
      paramLabel = "SRID",
      description = "The projection used in the database.")
  private int srid = 3857;

  @Override
  public Integer call() throws Exception {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.info("{} processors available.", Runtime.getRuntime().availableProcessors());

    DataSource datasource = PostgresHelper.datasource(database);
    PostgresCoordinateCache coordinateCache = new PostgresCoordinateCache(datasource);
    PostgresReferenceCache referenceCache = new PostgresReferenceCache(datasource);
    HeaderTable headerTable = new HeaderTable(datasource);
    NodeTable nodeTable = new NodeTable(datasource);
    WayTable wayTable = new WayTable(datasource);
    RelationTable relationTable = new RelationTable(datasource);

    BlobStore blobStore = options.blobStore();
    Set<Tile> tiles = new UpdateTask(
        blobStore,
        coordinateCache,
        referenceCache,
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        srid,
        zoom
    ).execute();

    logger.info("Saving differences");
    try (PrintWriter diffPrintWriter = new PrintWriter(blobStore.write(this.tiles))) {
      for (Tile tile : tiles) {
        diffPrintWriter.println(String.format("%d/%d/%d", tile.x(), tile.y(), tile.z()));
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

}
