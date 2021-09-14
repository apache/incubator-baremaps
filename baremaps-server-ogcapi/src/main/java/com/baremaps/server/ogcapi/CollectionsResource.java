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

package com.baremaps.server.ogcapi;

import com.baremaps.api.CollectionsApi;
import com.baremaps.model.Collection;
import com.baremaps.model.Collections;
import com.baremaps.model.Extent;
import com.baremaps.model.Link;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.array.SqlArrayType;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionsResource implements CollectionsApi {

  private static final Logger logger = LoggerFactory.getLogger(CollectionsResource.class);

  private static final QualifiedType<Extent> EXTENT =
      QualifiedType.of(Extent.class).with(Json.class);

  private final Jdbi jdbi;

  static ObjectMapper mapper = new ObjectMapper();

  @Inject
  public CollectionsResource(Jdbi jdbi) {
    this.jdbi = jdbi;
    this.jdbi.registerArrayType(new LinkArrayType());
    this.jdbi.registerArgument(new LinkArgumentFactory());
    this.jdbi.registerRowMapper(new CollectionMapper());
  }

  @Override
  public Response addCollection(Collection collection) {
    UUID collectionId = UUID.randomUUID();
    jdbi.useHandle(
        handle ->
            handle
                .createUpdate(
                    "insert into collections (id, title, description, links, extent, item_type, crs) values (:id, :title, :description, CAST(:links AS jsonb[]), CAST(:extent AS jsonb), :item_type, :crs)") //
                .bind("id", collectionId)
                .bind("title", collection.getTitle())
                .bind("description", collection.getDescription())
                .bindArray("links", Link.class, collection.getLinks())
                .bindByType("extent", collection.getExtent(), EXTENT)
                .bind("item_type", collection.getItemType())
                .bindArray("crs", String.class, collection.getCrs())
                .execute());
    return Response.created(URI.create("collections/" + collectionId)).build();
  }

  @Override
  public Response deleteCollection(UUID collectionId) {
    jdbi.useHandle(
        handle -> handle.execute("delete from collections where id = (?)", collectionId));

    return Response.noContent().build();
  }

  @Override
  public Response getCollection(UUID collectionId) {
    Collection collection =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "select id, title, description, links, extent, item_type, crs from collections where id = :id")
                    .bind("id", collectionId)
                    .map(new CollectionMapper())
                    .one());

    return Response.ok(collection).build();
  }

  @Override
  public Response getCollections() {
    List<Collection> collectionList =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "select id, title, description, links, extent, item_type, crs from collections")
                    .map(new CollectionMapper())
                    .list());

    Collections collections = new Collections();
    collections.setCollections(collectionList);

    return Response.ok(collections).build();
  }

  @Override
  public Response updateCollection(UUID collectionId, Collection collection) {
    jdbi.useHandle(
        handle ->
            handle
                .createUpdate(
                    "update collections set title = :title, description = :description, links = cast(:links as jsonb[]), extent = cast(:extent as jsonb), item_type = :item_type, crs = :crs where id = :id")
                .bind("title", collection.getTitle())
                .bind("description", collection.getDescription())
                .bindArray("links", Link.class, collection.getLinks())
                .bindByType("extent", collection.getExtent(), EXTENT)
                .bind("item_type", collection.getItemType())
                .bindArray("crs", String.class, collection.getCrs())
                .bind("id", collectionId)
                .execute());

    return Response.noContent().build();
  }

  static class LinkArgumentFactory extends AbstractArgumentFactory<Link> {

    LinkArgumentFactory() {
      super(Types.VARCHAR);
    }

    @Override
    protected Argument build(Link value, ConfigRegistry config) {
      return (position, statement, ctx) -> statement.setString(position, value.toString());
    }
  }

  static class LinkArrayType implements SqlArrayType<Link> {

    @Override
    public String getTypeName() {
      return "text";
    }

    @Override
    public Object convertArrayElement(Link link) {
      try {
        return mapper.writeValueAsString(link);
      } catch (JsonProcessingException e) {
        logger.error("An error occurred", e);
        return null;
      }
    }
  }

  static class CollectionMapper implements RowMapper<Collection> {
    @Override
    public Collection map(ResultSet rs, StatementContext ctx) throws SQLException {
      Collection collection = new Collection();
      collection.setId(UUID.fromString(rs.getString("id")));
      collection.setTitle(rs.getString("title"));
      collection.setDescription(rs.getString("description"));
      collection.setLinks(
          Arrays.stream((String[]) rs.getArray("links").getArray())
              .map(
                  link -> {
                    try {
                      return mapper.readValue(link, Link.class);
                    } catch (JsonProcessingException e) {
                      logger.error("An error occurred", e);
                      return null;
                    }
                  })
              .filter(Objects::nonNull)
              .collect(Collectors.toList()));
      collection.setExtent((Extent) rs.getObject("extent"));
      collection.setItemType(rs.getString("item_type"));
      collection.setCrs(Arrays.asList((String[]) rs.getArray("crs").getArray()));

      return collection;
    }
  }
}
