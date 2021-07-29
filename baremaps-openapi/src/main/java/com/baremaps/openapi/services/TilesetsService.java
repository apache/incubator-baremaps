package com.baremaps.openapi.services;

import com.baremaps.api.TilesetsApi;
import com.baremaps.model.TileSet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

public class TilesetsService implements TilesetsApi {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final QualifiedType<TileSet> TILESET = QualifiedType.of(TileSet.class).with(Json.class);

  private final Jdbi jdbi;

  @Inject
  public TilesetsService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void addTileset(TileSet tileSet) {
    UUID tilesetId = UUID.randomUUID(); // TODO: Read from body
    jdbi.useHandle(handle -> {
      handle.createUpdate("insert into tilesets (id, tileset) values (:id, CAST(:json AS JSONB))")
          .bind("json", serialize(tileSet))
          .bind("id", tilesetId)
          .execute();
    });
  }

  @Override
  public void deleteTileset(UUID tilesetId) {
    jdbi.useHandle(handle -> {
      handle.execute("delete from tilesets where id = (?)", tilesetId);
    });
  }

  @Override
  public TileSet getTileset(UUID tilesetId) {
    TileSet tileset = jdbi.withHandle(handle ->
        handle.createQuery("select tileset from tilesets where id = :id")
            .bind("id", tilesetId)
            .mapTo(TILESET)
            .one());
    return tileset;
  }

  @Override
  public List<UUID> getTilesets() {
    List<UUID> tilesets = jdbi.withHandle(handle ->
        handle.createQuery("select id from tilesets")
            .mapTo(UUID.class)
            .list());
    return tilesets;
  }

  @Override
  public void updateTileset(UUID tilesetId, TileSet tileSet) {
    jdbi.useHandle(handle -> {
      handle.createUpdate("update tilesets set tileset = cast(:json as jsonb) where id = :id")
          .bind("json", serialize(tileSet))
          .bind("id", tilesetId)
          .execute();
    });
  }

  private String serialize(TileSet tileSet) {
    try {
      return MAPPER.writeValueAsString(tileSet);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Unable serialize the tileset");
    }
  }

}
