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
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.baremaps.geocoder.GeonamesQueryBuilder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource that provides access to the geocoder.
 */
@Singleton
@javax.ws.rs.Path("/")
public class GeocoderResource {

  private static final Logger logger = LoggerFactory.getLogger(GeocoderResource.class);

  record GeocoderResponse(List<GeocoderResult> results) {
  }


  record GeocoderResult(float score, Map<String, Object> data) {
  }


  private final SearcherManager searcherManager;

  @Inject
  public GeocoderResource(SearcherManager searcherManager) {
    this.searcherManager = searcherManager;
  }

  @GET
  @javax.ws.rs.Path("/api/geocoder")
  public Response searchLocations(
      @QueryParam("queryText") String queryText,
      @QueryParam("countryCode") @DefaultValue("") String countryCode,
      @QueryParam("limit") @DefaultValue("10") int limit) {
    if (queryText == null) {
      throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
          .entity("The queryText parameter is mandatory").build());
    }
    try {
      IndexSearcher searcher = searcherManager.acquire();
      try {
        // Querying to search location uses AND operator between terms such as every term "adds up"
        // Examples of queryText:
        // - "paris", returns paris in france in first results (i.e because of scoring with
        // population)
        // - "paris brazil", returns paris in brazil and not paris in france.
        var query = new GeonamesQueryBuilder()
            .queryText(queryText).countryCode(countryCode).withScoringByPopulation()
            .withAndOperator()
            .build();

        var result = searcher.search(query, limit);
        var results =
            Arrays.stream(result.scoreDocs).map(scoreDoc -> asResult(searcher, scoreDoc)).toList();
        return Response.status(Response.Status.OK).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
            .header(CONTENT_TYPE, APPLICATION_JSON).entity(new GeocoderResponse(results)).build();
      } catch (IllegalArgumentException e) {
        logger.warn("Illegal input while processing request", e);
        return Response.status(Response.Status.BAD_REQUEST).build();
      } catch (IOException | ParseException e) {
        logger.error("Error while processing request", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      } finally {
        searcherManager.release(searcher);
      }
    } catch (IOException e) {
      logger.error("Error while processing request", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
}
