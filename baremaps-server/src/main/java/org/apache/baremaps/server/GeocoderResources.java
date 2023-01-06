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

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import org.apache.baremaps.geocoder.GeonamesQueryBuilder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;


@Singleton
@javax.ws.rs.Path("/")
public class GeocoderResources {

  record GeocoderResponse(List<GeocoderResult> results) {}


  record GeocoderResult(float score, Map<String, Object> data) {}


  private final SearcherManager searcherManager;

  @Inject
  public GeocoderResources(SearcherManager searcherManager) {
    this.searcherManager = searcherManager;
  }

  @GET
  @javax.ws.rs.Path("/api/geocoder")
  public Response getIpToLocation(@QueryParam("queryText") String queryText,
      @QueryParam("countryCode") @DefaultValue("") String countryCode,
      @QueryParam("limit") @DefaultValue("10") int limit) throws IOException {
    if (queryText == null) {
      throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
          .entity("The queryText parameter is mandatory").build());
    }
    var query = new GeonamesQueryBuilder().queryText(queryText).countryCode(countryCode).build();
    var searcher = searcherManager.acquire();
    try {
      var result = searcher.search(query, limit);
      var results =
          Arrays.stream(result.scoreDocs).map(scoreDoc -> asResult(searcher, scoreDoc)).toList();
      return Response.status(200).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
          .header(CONTENT_TYPE, APPLICATION_JSON).entity(new GeocoderResponse(results)).build();
    } catch (IllegalArgumentException e) {
      return Response.status(400).entity(e.getMessage()).build();
    } catch (IOException e) {
      return Response.status(500).entity(e.getMessage()).build();
    } finally {
      searcherManager.release(searcher);
    }
  }

  private GeocoderResult asResult(IndexSearcher indexSearcher, ScoreDoc scoreDoc) {
    try {
      var document = indexSearcher.doc(scoreDoc.doc);
      var data = new HashMap<String, Object>();
      for (var field : document.getFields()) {
        if (field.numericValue() != null) {
          data.put(field.name(), field.numericValue());
        } else if (field.stringValue() != null) {
          data.put(field.name(), field.stringValue());
        }
      }
      return new GeocoderResult(scoreDoc.score, data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @GET
  @javax.ws.rs.Path("/{path:.*}")
  public Response get(@PathParam("path") String path) {
    if (path.equals("") || path.endsWith("/")) {
      path += "index.html";
    }
    path = String.format("geocoder/%s", path);
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
      var bytes = inputStream.readAllBytes();
      return Response.ok().entity(bytes).build();
    } catch (IOException e) {
      return Response.status(404).build();
    }
  }
}
