package com.baremaps.openapi.services;

import com.baremaps.api.TilesetsApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.jackson2.Jackson2Config;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.json.Json;
import org.jdbi.v3.postgres.PostgresPlugin;

public class TilesetsService implements TilesetsApi {

  private Jdbi jdbi;

  public TilesetsService() {
    this.jdbi = Jdbi.create("jdbc:postgresql://localhost:5432/baremaps?user=baremaps&password=baremaps");
    this.jdbi.installPlugin(new PostgresPlugin());
    this.jdbi.installPlugin(new Jackson2Plugin());
//    ObjectMapper myObjectMapper = new ObjectMapper();
//    this.jdbi.getConfig(Jackson2Config.class).setMapper(myObjectMapper);
  }

QualifiedType<Object> qualifiedType = QualifiedType.of(Object.class).with(Json.class);

  @Override
  public void addTileset(Object body) {
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

//  @Json
  @Override
  public Object getTileset(String tilesetId) {
    Object tileset = jdbi.withHandle(handle ->
        handle.createQuery("select tileset::json from tilesets where id = ?")
            .bind(1, tilesetId)
            .mapTo(qualifiedType));
    return tileset;
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
  public void updateTileset(String tilesetId, Object body) {
    jdbi.useHandle(handle -> {
      handle.execute("insert into tilesets (id, tileset) values (?, ?)", tilesetId, body);
    });
  }
}
