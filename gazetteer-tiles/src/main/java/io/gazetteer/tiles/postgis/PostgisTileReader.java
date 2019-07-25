package io.gazetteer.tiles.postgis;

import io.gazetteer.tiles.Tile;
import io.gazetteer.tiles.TileException;
import io.gazetteer.tiles.TileReader;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.dbcp2.PoolingDataSource;

public class PostgisTileReader implements TileReader {

  private final PoolingDataSource datasource;

  private final PostgisConfig config;

  public PostgisTileReader(PoolingDataSource datasource, PostgisConfig config) {
    this.datasource = datasource;
    this.config = config;
  }

  @Override
  public byte[] read(Tile tile) throws TileException {
    try (Connection connection = datasource.getConnection();
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(data)) {
      for (PostgisLayer layer : config.getLayers()) {
        if (tile.getZ() >= layer.getMinZoom() && tile.getZ() <= layer.getMaxZoom()) {
          String sql = PostgisQueryBuilder.build(tile, layer);
          try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(sql);
            if (result.next()) {
              gzip.write(result.getBytes(1));
            }
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
