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

package com.baremaps.openapi.services;

import com.baremaps.api.StylesApi;
import com.baremaps.model.Link;
import com.baremaps.model.MbStyle;
import com.baremaps.model.StyleSet;
import com.baremaps.model.StyleSetEntry;
import com.baremaps.openapi.BaseService;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

public class StylesService extends BaseService implements StylesApi {

  private static final QualifiedType<MbStyle> MBSTYLE =
      QualifiedType.of(MbStyle.class).with(Json.class);

  private final Jdbi jdbi;

  @Inject
  public StylesService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Response addStyle(MbStyle mbStyle) {
    UUID styleId = UUID.randomUUID(); // TODO: Read from body
    jdbi.useHandle(
        handle ->
            handle
                .createUpdate("insert into styles (id, style) values (:id, CAST(:json AS jsonb))")
                .bindByType("json", mbStyle, MBSTYLE)
                .bind("id", styleId)
                .execute());

    return Response.created(URI.create("styles/" + styleId)).build();
  }

  @Override
  public Response deleteStyle(UUID styleId) {
    jdbi.useHandle(handle -> handle.execute("delete from styles where id = (?)", styleId));

    return Response.noContent().build();
  }

  @Override
  public Response getStyle(UUID styleId) {
    MbStyle style =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery("select style from styles where id = :id")
                    .bind("id", styleId)
                    .mapTo(MBSTYLE)
                    .one());

    return Response.ok(style).build();
  }

  @Override
  public Response getStyleSet() {
    List<UUID> ids =
        jdbi.withHandle(
            handle -> handle.createQuery("select id from styles").mapTo(UUID.class).list());

    StyleSet styleSet = new StyleSet();
    List<StyleSetEntry> entries = new ArrayList<>();

    for (UUID id : ids) {
      Link link = new Link();
      link.setHref(
          "http://localhost:8080/styles/"
              + id); // TODO: set dynamically from server or where the server gets it from
      link.setType("application/vnd.mapbox.style+json");
      link.setRel("stylesheet");

      StyleSetEntry entry = new StyleSetEntry();
      entry.setId(id);
      entry.setLinks(List.of(link));

      entries.add(entry);
    }

    styleSet.setStyles(entries);

    return Response.ok(styleSet).build();
  }

  @Override
  public Response updateStyle(UUID styleId, MbStyle mbStyle) {
    jdbi.useHandle(
        handle ->
            handle
                .createUpdate("update styles set style = :json where id = :id")
                .bindByType("json", mbStyle, MBSTYLE)
                .bind("id", styleId)
                .execute());

    return Response.noContent().build();
  }
}
