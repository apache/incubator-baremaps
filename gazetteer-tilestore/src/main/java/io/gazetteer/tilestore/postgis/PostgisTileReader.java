package io.gazetteer.tilestore.postgis;

import io.gazetteer.tilestore.Tile;
import io.gazetteer.tilestore.TileException;
import io.gazetteer.tilestore.TileReader;
import io.gazetteer.tilestore.XYZ;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class PostgisTileReader implements TileReader {

  private final String DATABASE = "jdbc:postgresql://localhost:5432/osm?user=osm&password=osm";

  private final List<PostgisLayer> layers;

  public PostgisTileReader(List<PostgisLayer> layers) {
    this.layers = layers;
  }

  @Override
  public Tile read(XYZ xyz) throws TileException {
    try (Connection connection = DriverManager.getConnection(DATABASE);
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        GZIPOutputStream tile = new GZIPOutputStream(data)) {
      for (PostgisLayer layer : layers) {
        if (xyz.getZ() >= layer.getMinZoom() && xyz.getZ() <= layer.getMaxZoom()) {
          String sql = PostgisQueryBuilder.build(xyz, layer);
          Statement statement = connection.createStatement();
          ResultSet result = statement.executeQuery(sql);
          result.next();
          tile.write(result.getBytes(1));
        }
      }
      tile.close();
      return new Tile(data.toByteArray());
    } catch (Exception e) {
      throw new TileException(e);
    }
  }

}
