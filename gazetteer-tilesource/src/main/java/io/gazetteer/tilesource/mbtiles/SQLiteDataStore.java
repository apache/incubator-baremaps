package io.gazetteer.tilesource.mbtiles;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.gazetteer.tilesource.Tile;
import io.gazetteer.tilesource.TileException;
import io.gazetteer.tilesource.TileSource;
import io.gazetteer.tilesource.TileTarget;
import io.gazetteer.tilesource.XYZ;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class SQLiteDataStore implements TileSource, TileTarget {

  public final org.sqlite.SQLiteDataSource dataSource;

  public final Map<String, String> metadata;

  public SQLiteDataStore(org.sqlite.SQLiteDataSource dataSource, Map<String, String> metadata) {
    this(dataSource, metadata, 10000);
  }

  public SQLiteDataStore(
      org.sqlite.SQLiteDataSource dataSource, Map<String, String> metadata, int cacheSize) {
    this.dataSource = dataSource;
    this.metadata = metadata;

  }

  @Override
  public Tile getTile(XYZ xyz) throws TileException {
    try (Connection connection = dataSource.getConnection()) {
      return SQLiteUtil.getTile(connection, xyz);
    } catch (SQLException e) {
      throw new TileException(e);
    }
  }

  public static SQLiteDataStore fromDataSource(org.sqlite.SQLiteDataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      Map<String, String> metadata = SQLiteUtil.getMetadata(connection);
      return new SQLiteDataStore(dataSource, metadata);
    }
  }

  @Override
  public void setTile(XYZ xyz, Tile tile) throws TileException {
    try (Connection connection = dataSource.getConnection()) {
      SQLiteUtil.setTile(connection, xyz, tile);
    } catch (SQLException e) {
      throw new TileException(e);
    }
  }
}
