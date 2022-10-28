/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.ogcapi;

import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.core.Response;
import org.apache.baremaps.api.TilesetsApi;
import org.apache.baremaps.database.tile.PostgresQuery;
import org.apache.baremaps.database.tile.PostgresTileStore;
import org.apache.baremaps.database.tile.Tile;
import org.apache.baremaps.database.tile.TileStore;
import org.apache.baremaps.database.tile.TileStoreException;
import org.apache.baremaps.model.TileJSON;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TilesetsResource implements TilesetsApi {

  private static final Logger logger = LoggerFactory.getLogger(TilesetsResource.class);

  private static final QualifiedType<TileJSON> TILESET =
      QualifiedType.of(TileJSON.class).with(Json.class);

  private final DataSource dataSource;

  private final Jdbi jdbi;

  private final LoadingCache<UUID, TileStore> tileStores =
      Caffeine.newBuilder().build(this::loadTileStore);

  @Inject
  public TilesetsResource(DataSource dataSource, Jdbi jdbi) {
    this.dataSource = dataSource;
    this.jdbi = jdbi;
  }

  private TileStore loadTileStore(UUID tilesetId) {
    TileJSON tileset =
        jdbi.withHandle(handle -> handle.createQuery("select tileset from tilesets where id = :id")
            .bind("id", tilesetId).mapTo(TILESET).one());
    List<PostgresQuery> queries = tileset.getVectorLayers().stream()
        .flatMap(layer -> layer.getQueries().stream().map(query -> new PostgresQuery(layer.getId(),
            query.getMinzoom(), query.getMaxzoom(), query.getSql())))
        .toList();
    return new PostgresTileStore(dataSource, queries);
  }

  @Override
  public Response addTileset(TileJSON tileSet) {
    UUID tilesetId;
    try {
      tilesetId = UUID.fromString(tileSet.getTiles().get(0).split("/")[4]);
    } catch (Exception e) {
      tilesetId = UUID.randomUUID();
    }
    UUID finalTilesetId = tilesetId;
    jdbi.useHandle(handle -> handle
        .createUpdate("insert into tilesets (id, tileset) values (:id, CAST(:json AS JSONB))")
        .bindByType("json", tileSet, TILESET).bind("id", finalTilesetId).execute());
    return Response.created(URI.create("tilesets/" + tilesetId)).build();
  }

  @Override
  public Response deleteTileset(UUID tilesetId) {
    tileStores.invalidate(tilesetId);
    jdbi.useHandle(handle -> handle.execute("delete from tilesets where id = (?)", tilesetId));
    return Response.noContent().build();
  }

  @Override
  public Response getTileset(UUID tilesetId) {
    TileJSON tileset =
        jdbi.withHandle(handle -> handle.createQuery("select tileset from tilesets where id = :id")
            .bind("id", tilesetId).mapTo(TILESET).one());
    return Response.ok(tileset).build();
  }

  @Override
  public Response getTilesets() {
    List<UUID> ids = jdbi.withHandle(
        handle -> handle.createQuery("select id from tilesets").mapTo(UUID.class).list());
    return Response.ok(ids).build();
  }

  @Override
  public Response updateTileset(UUID tilesetId, TileJSON tileSet) {
    tileStores.invalidate(tilesetId);
    jdbi.useHandle(handle -> handle
        .createUpdate("update tilesets set tileset = cast(:json as jsonb) where id = :id")
        .bindByType("json", tileSet, TILESET).bind("id", tilesetId).execute());
    return Response.noContent().build();
  }

  @Override
  public Response getTile(UUID tilesetId, String tileMatrixSetId, Integer tileMatrix,
      Integer tileRow, Integer tileCol) {
    Tile tile = new Tile(tileCol, tileRow, tileMatrix);
    TileStore tileStore = tileStores.get(tilesetId);
    try {
      return Response.ok(tileStore.read(tile)).header(CONTENT_ENCODING, "gzip").build();
    } catch (TileStoreException e) {
      return Response.serverError().build();
    }
  }
}
