package io.gazetteer.tilesource.postgis;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.gazetteer.tilesource.Tile;
import io.gazetteer.tilesource.TileSource;
import io.gazetteer.tilesource.XYZ;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

public class PostgisTileSource implements TileSource {

  private final String DATABASE = "jdbc:postgresql://localhost:5432/osm?user=osm&password=osm";

  private final AsyncLoadingCache<XYZ, Tile> cache;

  private final List<PostgisLayer> layers;

  public PostgisTileSource(List<PostgisLayer> layers) {
    this.layers = layers;
    this.cache =
        Caffeine.newBuilder()
            .maximumSize(10000)
            .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
            .buildAsync(xyz -> loadTile(xyz));
  }

  @Override
  public String getStyle() {
    return null;
  }

  @Override
  public CompletableFuture<Tile> getTile(XYZ xyz) {
    return cache.get(xyz);
  }

  private Tile loadTile(XYZ xyz) throws IOException, SQLException {
    try (ByteArrayOutputStream data = new ByteArrayOutputStream();
        GZIPOutputStream tile = new GZIPOutputStream(data)) {
      for (PostgisLayer layer : layers) {
        if (xyz.getZ() >= layer.getMinZoom() && xyz.getZ() <= layer.getMaxZoom()) {
          tile.write(loadLayer(xyz, layer));
        }
      }
      tile.close();
      return new Tile(data.toByteArray());
    }
  }

  private byte[] loadLayer(XYZ xyz, PostgisLayer layer) throws SQLException {
    try (Connection connection = DriverManager.getConnection(DATABASE)) {
      String sql = PostgisQueryBuilder.build(xyz, layer);
      Statement statement = connection.createStatement();
      ResultSet result = statement.executeQuery(sql);
      result.next();
      return result.getBytes(1);
    }
  }
}
