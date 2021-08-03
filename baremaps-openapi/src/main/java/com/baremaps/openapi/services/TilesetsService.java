package com.baremaps.openapi.services;

import com.baremaps.api.TilesetsApi;
import com.baremaps.model.TileSet;
import com.baremaps.openapi.TilesetQueryParser;
import com.baremaps.tile.Tile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import javax.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

public class TilesetsService implements TilesetsApi {

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
    jdbi.useHandle(handle -> {
      handle.createUpdate("insert into tilesets (id, tileset) values (:id, CAST(:json AS JSONB))")
          .bindByType("json", tileSet, TILESET)
          .bind("id", tilesetId)
          .execute();
    });
  }

  @Override
  public void deleteTileset(UUID tilesetId) {
    tilesets.remove(tilesetId);
    jdbi.useHandle(handle -> {
      handle.execute("delete from tilesets where id = (?)", tilesetId);
    });
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
    jdbi.useHandle(handle -> {
      handle.createUpdate("update tilesets set tileset = cast(:json as jsonb) where id = :id")
          .bindByType("json", tileSet, TILESET)
          .bind("id", tilesetId)
          .execute();
    });
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

    try (ByteArrayOutputStream data = new ByteArrayOutputStream()) {

      String sql = TilesetQueryParser.parse(tileset, tile);
      byte[] bytes = jdbi.withHandle(handle -> handle.createQuery(sql).mapTo(byte[].class).one());

      GZIPOutputStream gzip = new GZIPOutputStream(data);
      gzip.write(bytes);
      gzip.close();

      return data.toByteArray();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return new byte[] {};
  }
}
