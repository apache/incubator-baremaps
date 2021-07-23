package com.baremaps.cli;

import com.baremaps.blob.Blob;
import com.baremaps.blob.BlobStore;
import com.baremaps.osm.cache.CoordinateCache;
import com.baremaps.osm.cache.ReferenceCache;
import com.baremaps.osm.database.DiffService;
import com.baremaps.osm.database.HeaderTable;
import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.database.RelationTable;
import com.baremaps.osm.database.WayTable;
import com.baremaps.osm.postgres.PostgresCoordinateCache;
import com.baremaps.osm.postgres.PostgresHeaderTable;
import com.baremaps.osm.postgres.PostgresNodeTable;
import com.baremaps.osm.postgres.PostgresReferenceCache;
import com.baremaps.osm.postgres.PostgresRelationTable;
import com.baremaps.osm.postgres.PostgresWayTable;
import com.baremaps.postgres.jdbc.PostgresUtils;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "diff", description = "List the tiles affected by changes.")
public class Diff implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Diff.class);

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
      description = "The tiles affected by the update.",
      required = true)
  private URI tiles;

  @Option(
      names = {"--zoom"},
      paramLabel = "ZOOM",
      description = "The zoom level at which to compute the diff.")
  private int zoom = 12;

  @Option(
      names = {"--srid"},
      paramLabel = "SRID",
      description = "The projection used by the database.")
  private int srid = 3857;

  @Override
  public Integer call() throws Exception {
    BlobStore blobStore = options.blobStore();
    DataSource datasource = PostgresUtils.datasource(database);
    CoordinateCache coordinateCache = new PostgresCoordinateCache(datasource);
    ReferenceCache referenceCache = new PostgresReferenceCache(datasource);
    HeaderTable headerTable = new PostgresHeaderTable(datasource);
    NodeTable nodeTable = new PostgresNodeTable(datasource);
    WayTable wayTable = new PostgresWayTable(datasource);
    RelationTable relationTable = new PostgresRelationTable(datasource);

    logger.info("Saving diff");
    Path tmpTiles = Files.createFile(Paths.get("diff.tmp"));
    try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(tmpTiles))) {
      new DiffService(
          blobStore,
          coordinateCache,
          referenceCache,
          headerTable,
          nodeTable,
          wayTable,
          relationTable,
          srid,
          zoom
      ).call();
    }
    blobStore.put(this.tiles, Blob.builder()
        .withContentLength(Files.size(tmpTiles))
        .withInputStream(Files.newInputStream(tmpTiles))
        .build());
    Files.deleteIfExists(tmpTiles);

    logger.info("Done");

    return 0;
  }

}
