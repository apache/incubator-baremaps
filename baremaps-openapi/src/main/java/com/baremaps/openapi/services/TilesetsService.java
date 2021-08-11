package com.baremaps.openapi.services;

import com.baremaps.api.TilesetsApi;
import com.baremaps.model.TileSet;
import com.baremaps.openapi.TilesetQueryParser;
import com.baremaps.tile.Tile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TilesetsService implements TilesetsApi {

  private static final Logger logger = LoggerFactory.getLogger(TilesetsService.class);

  private static final QualifiedType<TileSet> TILESET = QualifiedType.of(TileSet.class).with(Json.class);

  private final Jdbi jdbi;

  private final HashMap<UUID, TileSet> tilesets = new HashMap<>();

  @Inject
  public TilesetsService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void addTileset(TileSet tileSet) {
    UUID tilesetId = UUID.randomUUID(); // TODO: Read from body
    jdbi.useHandle(
        handle -> handle.createUpdate("insert into tilesets (id, tileset) values (:id, CAST(:json AS JSONB))")
            .bindByType("json", tileSet, TILESET)
            .bind("id", tilesetId)
            .execute());
  }

  @Override
  public void deleteTileset(UUID tilesetId) {
    tilesets.remove(tilesetId);
    jdbi.useHandle(handle -> handle.execute("delete from tilesets where id = (?)", tilesetId));
  }

  @Override
  public TileSet getTileset(UUID tilesetId) {
    return jdbi.withHandle(handle ->
        handle.createQuery("select tileset from tilesets where id = :id")
            .bind("id", tilesetId)
            .mapTo(TILESET)
            .one());
  }

  @Override
  public List<UUID> getTilesets() {
    return jdbi.withHandle(handle ->
        handle.createQuery("select id from tilesets")
            .mapTo(UUID.class)
            .list());
  }

  @Override
  public void updateTileset(UUID tilesetId, TileSet tileSet) {
    tilesets.remove(tilesetId);
    jdbi.useHandle(handle -> handle.createUpdate("update tilesets set tileset = cast(:json as jsonb) where id = :id")
        .bindByType("json", tileSet, TILESET)
        .bind("id", tilesetId)
        .execute());
  }

  @Override
  public byte[] getTile(UUID tilesetId, String tileMatrixSetId, Integer tileMatrix, Integer tileRow, Integer tileCol) {
    TileSet tileset;
    if (tilesets.containsKey(tilesetId)) {
      tileset = tilesets.get(tilesetId);
    } else {
      tileset = getTileset(tilesetId);
      tilesets.put(tilesetId, tileset);
    }

    Tile tile = new Tile(tileCol, tileRow, tileMatrix);

    TilesetQueryParser parser = new TilesetQueryParser();
    String sql = parser.parse(tileset, tile);
    logger.debug("Executing query: {}", sql);

    try (Handle handle = jdbi.open();
        Connection connection = handle.getConnection();
        Statement statement = connection.createStatement();
        ByteArrayOutputStream data = new ByteArrayOutputStream()) {

      int length = 0;
      ResultSet resultSet = statement.executeQuery(sql);
      while (resultSet.next()) {
        byte[] bytes = resultSet.getBytes(1);
        length += bytes.length;
        data.write(bytes);
      }
      handle.close();

      if (length > 0) {
        return data.toByteArray();
      }
    } catch (IOException | SQLException e) {
      e.printStackTrace();
    }

    return null;
  }
}
