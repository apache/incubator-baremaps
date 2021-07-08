package com.baremaps.openapi.services;

import com.baremaps.api.TilesetsApi;
import com.baremaps.model.TileSet;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.json.Json;
import org.jdbi.v3.postgres.PostgresPlugin;

public class TilesetsService implements TilesetsApi {

  private Jdbi jdbi;

  public TilesetsService() {
    this.jdbi = Jdbi.create("jdbc:postgresql://localhost:5432/baremaps?user=baremaps&password=baremaps");
    this.jdbi.installPlugin(new PostgresPlugin());
    this.jdbi.installPlugin(new Jackson2Plugin());
  }

  @Override
  public void addTileset(TileSet body) {
    UUID tilesetId = UUID.randomUUID(); // TODO: Read from body
    jdbi.useHandle(handle -> {
      handle.execute("insert into tilesets (id, tileset) values (?, ?)", tilesetId, body);
    });
  }

  @Override
  public void deleteTileset(String tilesetId) {
    jdbi.useHandle(handle -> {
      handle.execute("delete from tilesets where id = (?)", tilesetId);
    });
  }

  @Override
  public TileSet getTileset(String tilesetId) {
    QualifiedType<TileSet> qualifiedType = QualifiedType.of(TileSet.class).with(Json.class);

    TileSet tilesets = jdbi.withHandle(handle ->
        handle.createQuery("select tileset from tilesets where id = :id")
            .bind("id", tilesetId)
            .mapTo(qualifiedType)
            .one());
    return tilesets;
  }

  @Override
  public List<String> getTilesets() {
    List<String> tilesets = jdbi.withHandle(handle ->
        handle.createQuery("select id from tilesets")
            .mapTo(String.class)
            .list());
    return tilesets;
  }

  @Override
  public void updateTileset(String tilesetId, TileSet body) {
    jdbi.useHandle(handle -> {
      handle.execute("insert into tilesets (id, tileset) values (?, ?)", tilesetId, body);
    });
  }
}
