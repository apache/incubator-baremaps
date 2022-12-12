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
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.baremaps.geocoder.Geocoder;
import org.apache.baremaps.geocoder.request.Request;
import org.apache.lucene.queryparser.classic.ParseException;

@Singleton
@javax.ws.rs.Path("/")
public class GeocoderResources {

  private final Geocoder geocoder;

  @Inject
  public GeocoderResources(Geocoder geocoder) {
    this.geocoder = geocoder;
  }

  @GET
  @javax.ws.rs.Path("/api/geocoder")
  public Response getIpToLocation(@QueryParam("address") String address) {
    if (address == null) {
      throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
          .entity("address parameter is mandatory").build());
    }

    try {
      var request = new Request(address, 20);
      var response = geocoder.search(request);
      return Response.status(200).header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
          .header(CONTENT_TYPE, APPLICATION_JSON).entity(response).build();
    } catch (IllegalArgumentException e) {
      return Response.status(400).entity(e.getMessage()).build();
    } catch (IOException | ParseException e) {
      return Response.status(500).entity(e.getMessage()).build();
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
