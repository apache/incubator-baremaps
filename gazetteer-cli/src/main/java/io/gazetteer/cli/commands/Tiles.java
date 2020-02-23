package io.gazetteer.cli.commands;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.google.common.base.Stopwatch;
import io.gazetteer.core.io.InputStreams;
import io.gazetteer.core.postgis.PostgisHelper;
import io.gazetteer.tiles.Tile;
import io.gazetteer.tiles.TileReader;
import io.gazetteer.tiles.TileWriter;
import io.gazetteer.tiles.config.Config;
import io.gazetteer.tiles.file.FileTileStore;
import io.gazetteer.tiles.postgis.SimpleTileReader;
import io.gazetteer.tiles.postgis.WithTileReader;
import io.gazetteer.tiles.s3.S3TileStore;
import io.gazetteer.tiles.util.TileUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "tiles")
public class Tiles implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Tiles.class);

  @Parameters(
      index = "0",
      paramLabel = "CONFIG_FILE",
      description = "The YAML configuration file.")
  private String file;

  @Parameters(
      index = "1",
      paramLabel = "POSTGRES_DATABASE",
      description = "The Postgres database.")
  private String database;

  @Parameters(
      index = "2",
      paramLabel = "TILE_REPOSITORY",
      description = "The tile repository.")
  private String repository;

  @Option(
      names = {"--minZoom"},
      description = "The minimal zoom level.")
  private int minZoom = 0;

  @Option(
      names = {"--maxZoom"},
      description = "The maximal zoom level.")
  private int maxZoom = 14;

  @Option(
      names = {"-t", "--tile-reader"},
      description = "The tile reader.")
  private String tileReader = "simple";

  private TileReader tileReader(PoolingDataSource dataSource, Config config) {
    switch (tileReader) {
      case "simple":
        return new SimpleTileReader(dataSource, config);
      case "with":
        return new WithTileReader(dataSource, config);
      default:
        throw new UnsupportedOperationException("Unsupported tile reader");
    }
  }

  private TileWriter tileWriter(String repository) throws IOException {
    if (Files.exists(Paths.get(repository))) {
      return new FileTileStore(Paths.get(repository));
    } else if (repository.startsWith("s3://")) {
      AmazonS3 client = AmazonS3ClientBuilder.standard().defaultClient();
      AmazonS3URI uri = new AmazonS3URI(repository);
      return new S3TileStore(client, uri);
    } else {
      throw new IOException("");
    }
  }

  @Override
  public Integer call() throws SQLException, ParseException, IOException {
    // Read the configuration toInputStream
    Config config = Config.load(InputStreams.from(file));
    PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);

    // Initialize tile reader and writer
    TileReader tileReader = tileReader(datasource, config);
    TileWriter tileWriter = tileWriter(repository);

    try (Connection connection = datasource.getConnection()) {
      Geometry geometry = TileUtil.bbox(connection);
      Stream<Tile> coords = TileUtil.getTiles(geometry, minZoom, maxZoom);
      coords.forEach(tile -> {
        try {
          byte[] bytes = tileReader.read(tile);
          tileWriter.write(tile, bytes);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }

    return 0;
  }

}
