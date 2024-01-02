/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource that searches for entities in the database.
 */
@Singleton
@javax.ws.rs.Path("/")
public class SearchResource {

  private static final Logger logger = LoggerFactory.getLogger(SearchResource.class);

  private final String SEARCH_QUERY =
      """
          SELECT id, tags, st_asewkt(st_transform(geom, 'EPSG:4326')), ts_rank_cd(to_tsvector('english', tags), query) as rank
          FROM osm_entities, phraseto_tsquery('english', ?) as query
          WHERE to_tsvector('english', tags) @@ query
          ORDER BY rank DESC
          LIMIT ?;
          """;

  private final DataSource dataSource;

  @Inject
  public SearchResource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  record SearchResponse(List<SearchResult> results) {
  }

  record SearchResult(long id, JsonNode tags, String wkt, double score) {
  }

  @GET
  @javax.ws.rs.Path("/api/search")
  @Produces(MediaType.APPLICATION_JSON)
  public Response search(
      @QueryParam("query") String queryText,
      @QueryParam("limit") @DefaultValue("10") int limit) {
    try (var connection = dataSource.getConnection()) {
      var statement = connection.prepareStatement(SEARCH_QUERY);
      statement.setString(1, queryText);
      statement.setInt(2, limit);
      var result = statement.executeQuery();
      var list = new ArrayList<SearchResult>();
      while (result.next()) {
        var id = result.getLong(1);
        var json = result.getString(2);
        var tags = new ObjectMapper().readTree(json);
        var wkt = result.getString(3);
        var rank = result.getDouble(4);
        list.add(new SearchResult(id, tags, wkt, rank));
      }
      var response = new SearchResponse(list);
      return Response.status(Response.Status.OK).entity(response).build();
    } catch (SQLException | JsonProcessingException e) {
      logger.error("Error while searching for {}", queryText, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}
