package io.gazetteer.tiles.postgis;

import io.gazetteer.tiles.Tile;
import io.gazetteer.tiles.TileException;
import io.gazetteer.tiles.TileReader;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.dbcp2.PoolingDataSource;

public class PostgisTileReader implements TileReader {

  private final PoolingDataSource datasource;

  private final List<PostgisLayer> layers;

  public PostgisTileReader(PoolingDataSource datasource, List<PostgisLayer> layers) {
    this.datasource = datasource;
    this.layers = layers;
  }

  @Override
  public byte[] read(Tile tile) throws TileException {
    try (Connection connection = datasource.getConnection();
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(data)) {
      for (PostgisLayer layer : layers) {
        if (tile.getZ() >= layer.getMinZoom() && tile.getZ() <= layer.getMaxZoom()) {
          String sql = PostgisQueryBuilder.build(tile, layer);
          try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(sql);
            result.next();
            gzip.write(result.getBytes(1));
          }
        }
      }
      gzip.close();
      return data.toByteArray();
    } catch (Exception e) {
      throw new TileException(e);
    }
  }

}
