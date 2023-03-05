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

package org.apache.baremaps.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mil.nga.sf.Geometry;
import org.apache.baremaps.database.repository.PostgresJsonbMapper;
import org.apache.baremaps.openstreetmap.utils.GeometryUtils;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.io.GeoJSONWriter;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Singleton
@Path("/")
public class SearchResources {

  private final DataSource dataSource;

  private final String searchQuery = """
          SELECT id, tags, ST_AsGeoJSON(geom) AS geom
          FROM  osm_named_entities
          WHERE osm_named_entities.tsv @@ to_tsquery('English', ?)
          ORDER BY ts_rank_cd(osm_named_entities.tsv, to_tsquery('English', ?)) DESC
          LIMIT 10
           """;

  @Inject
  public SearchResources(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  private record Feature(String id, Map<String, Object> properties, JsonNode geometry) {

  }

  @GET
  @Path("/search")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Feature> search(@QueryParam("query") String query) {
    try (Connection connection = dataSource.getConnection()) {
      var statement = connection.prepareStatement(searchQuery);
      statement.setString(1, query);
      statement.setString(2, query);
      var resultSet = statement.executeQuery();
      var results = new ArrayList<Feature>();
      while (resultSet.next()) {
        var id = resultSet.getLong("id");
        var tags = PostgresJsonbMapper.toMap(resultSet.getString("tags"));
        var json = resultSet.getString("geom");
        var geometry = new ObjectMapper().readTree(json);
        var feature = new Feature(String.valueOf(id), tags, geometry);
        results.add(feature);
      }
      return results;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
