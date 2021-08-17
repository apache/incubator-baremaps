package com.baremaps.openapi.services;

import com.baremaps.api.TilesetsApi;
import com.baremaps.model.TileSet;
import com.baremaps.openapi.TilesetQueryParser;
import com.baremaps.tile.Tile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
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
  public Response addTileset(TileSet tileSet) {
    UUID tilesetId;
    try {
      tilesetId = UUID.fromString(tileSet.getTiles().get(0).split("/")[4]);
    } catch (Exception e) {
      tilesetId = UUID.randomUUID();
    }

    UUID finalTilesetId = tilesetId;
    jdbi.useHandle(
        handle -> handle.createUpdate("insert into tilesets (id, tileset) values (:id, CAST(:json AS JSONB))")
            .bindByType("json", tileSet, TILESET)
            .bind("id", finalTilesetId)
            .execute());

    return Response.created(URI.create("tilesets/" + tilesetId)).build();
  }

  @Override
  public Response deleteTileset(UUID tilesetId) {
    tilesets.remove(tilesetId);
    jdbi.useHandle(handle -> handle.execute("delete from tilesets where id = (?)", tilesetId));

    return Response.noContent().build();
  }

  @Override
  public Response getTileset(UUID tilesetId) {
    TileSet tileset = jdbi.withHandle(handle ->
        handle.createQuery("select tileset from tilesets where id = :id")
            .bind("id", tilesetId)
            .mapTo(TILESET)
            .one());

    return Response.ok(tileset).build();
  }

  @Override
  public Response getTilesets() {
    List<UUID> ids = jdbi.withHandle(handle ->
        handle.createQuery("select id from tilesets")
            .mapTo(UUID.class)
            .list());

    return Response.ok(ids).build();
  }

  @Override
  public Response updateTileset(UUID tilesetId, TileSet tileSet) {
    tilesets.remove(tilesetId);
    jdbi.useHandle(handle -> handle.createUpdate("update tilesets set tileset = cast(:json as jsonb) where id = :id")
        .bindByType("json", tileSet, TILESET)
        .bind("id", tilesetId)
        .execute());

    return Response.noContent().build();
  }

  @Override
  public Response getTile(UUID tilesetId, String tileMatrixSetId, Integer tileMatrix, Integer tileRow,
      Integer tileCol) {
    TileSet tileset;
    if (tilesets.containsKey(tilesetId)) {
      tileset = tilesets.get(tilesetId);
    } else {
      tileset = jdbi.withHandle(handle ->
          handle.createQuery("select tileset from tilesets where id = :id")
              .bind("id", tilesetId)
              .mapTo(TILESET)
              .one());
      tileset.setTiles(
          List.of(String.format("http://localhost:8080/tilesets/%s/tiles/matrix-set-id/{z}/{y}/{x}", tilesetId)));
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
        return Response.ok(data.toByteArray()).build();
      }
    } catch (IOException | SQLException e) {
//      TODO: proper error handling
      return Response.serverError().build();
    }

    return null;
  }
}
