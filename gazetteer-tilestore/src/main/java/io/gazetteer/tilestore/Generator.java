package io.gazetteer.tilestore;

import io.gazetteer.tilestore.postgis.PostgisConfig;
import io.gazetteer.tilestore.postgis.PostgisLayer;
import io.gazetteer.tilestore.postgis.PostgisTileReader;
import io.gazetteer.tilestore.util.TileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import org.locationtech.jts.geom.Geometry;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(description = "Generate vector tiles from Postgresql")
public class Generator implements Runnable {

  @Parameters(index = "0", paramLabel = "CONFIG_FILE", description = "The YAML configuration config.")
  private Path config;

  @Parameters(index = "1", paramLabel = "POSTGRES_DATABASE", description = "The Postgres database.")
  private String database;

  @Parameters(index = "2", paramLabel = "TILE_DIRECTORY", description = "The tile directory.")
  private File directory;

  @Override
  public void run() {
    try {
      // Read the configuration file
      List<PostgisLayer> layers = PostgisConfig.load(new FileInputStream(config.toFile())).getLayers();
      TileReader tileReader = new PostgisTileReader(database, layers);
      try (Connection connection = DriverManager.getConnection(database)) {
        Geometry geometry = TileUtil.bbox(connection);
        List<XYZ> coords = TileUtil.overlappingXYZ(geometry, 1, 14);
        for (XYZ xyz : coords) {
          Tile tile = tileReader.read(xyz);
          Path path = directory.toPath()
              .resolve(Integer.toString(xyz.getZ()))
              .resolve(Integer.toString(xyz.getX()));
          Files.createDirectories(path);
          Path file = path.resolve(Integer.toString(xyz.getY()));
          Files.write(file, tile.getBytes());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    CommandLine.run(new Generator(), args);
  }

}
