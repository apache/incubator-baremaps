/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.openapi.resources;

import com.baremaps.api.MapsApi;
import com.baremaps.model.MapMetadata;
import com.baremaps.model.MapsMetadata;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

public class MapsService implements MapsApi {

  private static final QualifiedType<MapMetadata> MAP =
      QualifiedType.of(MapMetadata.class).with(Json.class);

  private final Jdbi jdbi;

  @Inject
  public MapsService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Response addMap(MapMetadata map) {
    UUID mapId = UUID.randomUUID(); // TODO: Read from body
    jdbi.useHandle(
        handle ->
            handle
                .createUpdate("insert into maps (id, map) values (:id, CAST(:json AS jsonb))")
                .bindByType("json", map, MAP)
                .bind("id", mapId)
                .execute());
    return Response.created(URI.create("maps/" + mapId)).build();
  }

  @Override
  public Response deleteMap(UUID mapId) {
    jdbi.useHandle(handle -> handle.execute("delete from maps where id = (?)", mapId));
    return Response.noContent().build();
  }

  @Override
  public Response getMap(UUID mapId) {
    MapMetadata map =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery("select map from maps where id = :id")
                    .bind("id", mapId)
                    .mapTo(MAP)
                    .one());
    return Response.ok(map).build();
  }

  @Override
  public Response getMaps() {
    List<MapMetadata> maps =
        jdbi.withHandle(handle -> handle.createQuery("select map from maps").mapTo(MAP).list());
    return Response.ok(new MapsMetadata().mapsMetadata(maps)).build();
  }

  @Override
  public Response updateMap(UUID mapId, MapMetadata map) {
    jdbi.useHandle(
        handle ->
            handle
                .createUpdate("update maps set map = :json where id = :id")
                .bindByType("json", map, MAP)
                .bind("id", mapId)
                .execute());
    return Response.noContent().build();
  }
}
